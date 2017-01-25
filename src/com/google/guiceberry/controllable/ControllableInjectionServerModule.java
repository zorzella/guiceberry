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
package com.google.guiceberry.controllable;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.guiceberry.TestId;
import com.google.guiceberry.controllable.IcStrategy.ServerSupport;

import java.util.Map;

/**
 * This internal class is basically what the {@link IcMaster} uses to fulfill
 * its {@link IcMaster#buildServerModule(java.util.Collection)} method.
 * 
 * @author Luiz-Otavio Zorzella
 * @author Jesse Wilson
 */
final class ControllableInjectionServerModule extends AbstractModule {
  
  private final Map<Key<?>, IcStrategy> rewriter;
  
  public ControllableInjectionServerModule(Map<Key<?>, IcStrategy> rewriter) {
    this.rewriter = rewriter;
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  protected void configure() {
    for (Map.Entry<Key<?>, IcStrategy> e : rewriter.entrySet()) {
      bind(IcStrategy.wrap(IcServer.class, e.getKey()))
           .toProvider(new MyServerProvider(e.getKey(), getProvider(TestId.class), 
             getProvider(e.getValue().serverSupportClass())));
    }
  }

  private static final class MyServerProvider<T> implements Provider<IcServer<T>> {
    private final Key<T> key;
    private final Provider<IcStrategy.ServerSupport> serverSupportProvider;
    private final Provider<TestId> testIdProvider;
    
    public MyServerProvider(Key<T> key,
        Provider<TestId> testIdProvider, Provider<IcStrategy.ServerSupport> serverControllerSupportProvider) {
      this.key = key;
      this.testIdProvider = testIdProvider;
      this.serverSupportProvider = serverControllerSupportProvider;
    }

    public IcServer<T> get() {
      return new IcServer<T>() {     
        public T getOverride(Provider<? extends T> delegate) {
          ControllableId<T> controllableId = 
            new ControllableId<T>(testIdProvider.get(), key);
          ServerSupport serverSupport = serverSupportProvider.get();
          if (!serverSupport.isControlled(controllableId)) {
            return delegate.get();
          }
          return serverSupport.getOverride(controllableId, delegate);
        }
      };
    }
  }
}