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

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.testing.guiceberry.TestId;

import java.util.Map;

/**
 * This internal class is basically what the {@link IcMaster} uses to fulfill
 * its {@link IcMaster#buildClientModule()} method.
 * 
 * @author Luiz-Otavio Zorzella
 * @author Jesse Wilson
 */
final class ControllableInjectionClientModule extends AbstractModule {
  
  private final Map<Key<?>, IcStrategy> rewriter;
  
  public ControllableInjectionClientModule(Map<Key<?>, IcStrategy> rewriter) {
    this.rewriter = rewriter;
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  protected void configure() {
    for (Map.Entry<Key<?>, IcStrategy> e : rewriter.entrySet()) {
      bind(IcStrategy.wrap(IcClient.class, e.getKey()))
         .toProvider(new MyClientProvider(
             e.getKey(), 
             getProvider(TestId.class), 
             getProvider(e.getValue().clientSupportClass()),
             getProvider(TearDownAccepter.class)));
    }
  }

  private static final class MyClientProvider<T> implements Provider<IcClient<T>> {
    private final Key<T> key;
    private final Provider<IcStrategy.ClientSupport> clientControllerSupportProvider;
    private final Provider<TestId> testIdProvider;
    private final Provider<TearDownAccepter> tearDownAccepterProvider;
    
    public MyClientProvider(Key<T> key,  
        Provider<TestId> testIdProvider, 
        Provider<IcStrategy.ClientSupport> clientControllerSupportProvider,
        Provider<TearDownAccepter> tearDownAccepterProvider) {
      this.key = key;
      this.testIdProvider = testIdProvider;
      this.clientControllerSupportProvider = clientControllerSupportProvider;
      this.tearDownAccepterProvider = tearDownAccepterProvider;
    }

    public IcClient<T> get() {
      return new IcClient<T>() {     
        public void setOverride(T override) {
          if (override == null) {
            throw new NullPointerException();
          }
          final IcStrategy.ClientSupport icClientStrategy = 
            clientControllerSupportProvider.get();
          final ControllableId<T> controllableId = 
            new ControllableId<T>(testIdProvider.get(), key);
          tearDownAccepterProvider.get().addTearDown(new TearDown() {
            public void tearDown() throws Exception {
              icClientStrategy.resetOverride(controllableId);
            }
          });
          icClientStrategy.setOverride(controllableId, override);
        }

        public void resetOverride() {
          final IcStrategy.ClientSupport icClientStrategy = 
            clientControllerSupportProvider.get();
          final ControllableId<T> controllableId = 
            new ControllableId<T>(testIdProvider.get(), key);
          icClientStrategy.resetOverride(controllableId);
        }
      };
    }
  }
}