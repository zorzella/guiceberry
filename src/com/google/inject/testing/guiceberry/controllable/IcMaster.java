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

import java.util.Collection;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.internal.Maps;

public final class IcMaster {
  
  private final Map<Key<?>, IcStrategyCouple> controlledKeysToStrategy = Maps.newHashMap();
  
  public IcMaster thatControls(IcStrategyCouple support, 
      Class<?>... classes) {
    for (Class<?> clazz : classes) {
      Key<?> key = Key.get(clazz);
      if (controlledKeysToStrategy.containsKey(key)) {
        throw new IllegalArgumentException();
      }
      controlledKeysToStrategy.put(key, support);
    }
    return this;
  }

  public IcMaster thatControlsrewrite(IcStrategyCouple support, 
      Key<?>... keys) {
    for (Key<?> key : keys) {
      if (controlledKeysToStrategy.containsKey(key)) {
        throw new IllegalArgumentException();
      }
      controlledKeysToStrategy.put(key, support);
    }
    return this;
  }

  public Module buildClientModule() {
    return new ControllableInjectionClientModule(controlledKeysToStrategy);
  }
  
  private static class ProvisionInterceptorModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ProvisionInterceptor.class).to(ProvisionInterceptorModule.MyProvisionInterceptor.class);
    }
    
    private static class MyProvisionInterceptor implements ProvisionInterceptor {

      @Inject Injector injector;
      
      @SuppressWarnings("unchecked")
      public <T> T intercept(Key<T> key, Provider<? extends T> delegate) {
        IcServer<T> instance = (IcServer<T>) injector.getInstance(IcStrategyCouple.wrap(IcServer.class, key));
        return instance.getOverride(delegate);
      }
    }
  }

  public Module buildServerModule(final Collection<? extends Module> modules) {
    return new InterceptingBindingsBuilder()
      .install(modules)
      .install(new ProvisionInterceptorModule())
      .install(new ControllableInjectionServerModule(controlledKeysToStrategy))
      .intercept(controlledKeysToStrategy.keySet())
      .build();
  }
}