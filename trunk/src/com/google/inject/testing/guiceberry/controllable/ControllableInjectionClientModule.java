/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.inject.testing.guiceberry.controllable;

import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.controllable.IcStrategyCouple.IcClientStrategy;

/**
 * This internal class is basically what the {@link IcMaster} uses to fullfil
 * its {@link IcMaster#buildClientModule()} method.
 * 
 * @author Luiz-Otavio Zorzella
 * @author Jesse Wilson
 */
final class ControllableInjectionClientModule extends AbstractModule {
  
  private final Map<Key<?>, IcStrategyCouple> rewriter;
  
  public ControllableInjectionClientModule(Map<Key<?>, IcStrategyCouple> rewriter) {
    this.rewriter = rewriter;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  protected void configure() {
    for (Map.Entry<Key<?>, IcStrategyCouple> e : rewriter.entrySet()) {
      bind(IcStrategyCouple.wrap(IcClient.class, e.getKey()))
         .toProvider(new MyClientProvider(e.getKey(), getProvider(TestId.class), 
             getProvider(e.getValue().clientControllerClass())));
    }
  }

  private static final class MyClientProvider<T> implements Provider<IcClient<T>> {
    private final Key<T> key;
    private final Provider<IcClientStrategy> clientControllerSupportProvider;
    private final Provider<TestId> testIdProvider;
    
    public MyClientProvider(Key<T> key,  
        Provider<TestId> testIdProvider, Provider<IcClientStrategy> clientControllerSupportProvider) {
      this.key = key;
      this.testIdProvider = testIdProvider;
      this.clientControllerSupportProvider = clientControllerSupportProvider;
    }

    public IcClient<T> get() {
      return new IcClient<T>() {     
        @SuppressWarnings("unchecked")
        public void setOverride(T override) {
          if (override == null) {
            throw new NullPointerException();
          }
          clientControllerSupportProvider.get().setOverride(
              new ControllableId(testIdProvider.get(), key), override);
        }

        @SuppressWarnings("unchecked")
        public void resetOverride() {
          clientControllerSupportProvider.get().setOverride(
              new ControllableId(testIdProvider.get(), key), null);
        }
      };
    }
  }
}