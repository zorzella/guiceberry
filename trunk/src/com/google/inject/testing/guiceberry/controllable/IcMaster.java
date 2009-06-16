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

//TODO: document
/**
 * As documented at length in the tutorial for 
 * {@link tutorial_1_server.Example4CanonicalSameJvmControllableInjectionTest}
 * etc, Controllable Injections need to be set up through an {@link IcMaster}, 
 * which is to participate both in the building of the test Injector as well as
 * the server Injector.
 * 
 * <p>Both Injectors need to basically agree on two different things: the list
 * of classes/keys that are subject to being controlled and, for each of these
 * classes/keys the strategy ({@link IcStrategyCouple}) that is to be used.
 * 
 * <p>Then, the client module (built through {@link #buildClientModule()} is to 
 * be added to the test Injector's list of modules; and, on the server side, the
 * {@link #buildServerModule(Collection)} is to be used.
 * 
 * @author Luiz-Otavio Zorzella
 * @author Jesse Wilson
 */
public final class IcMaster {
  
  private final Map<Key<?>, IcStrategyCouple> controlledKeyToStrategyMap = 
    Maps.newHashMap();
  
  /**
   * Declares the given (un-annotated) {@code classes} as being subject to 
   * Controllable Injection with the given {@code strategy}.
   * 
   * @see #thatControls(IcStrategyCouple, Key...) 
   *  
   * @return itself, for method chaining
   */
  public IcMaster thatControls(IcStrategyCouple strategy, 
      Class<?>... classes) {
    for (Class<?> clazz : classes) {
      Key<?> key = Key.get(clazz);
      if (controlledKeyToStrategyMap.containsKey(key)) {
        throw new IllegalArgumentException();
      }
      controlledKeyToStrategyMap.put(key, strategy);
    }
    return this;
  }

  /**
   * Declares the given annotated classes (through they {@code keys}) as being 
   * subject to Controllable Injection with the given {@code strategy}.
   * 
   * @see #thatControls(IcStrategyCouple, Class...)
   * 
   * @return itself, for method chaining
   */
  public IcMaster thatControls(IcStrategyCouple support, 
      Key<?>... keys) {
    for (Key<?> key : keys) {
      if (controlledKeyToStrategyMap.containsKey(key)) {
        throw new IllegalArgumentException();
      }
      controlledKeyToStrategyMap.put(key, support);
    }
    return this;
  }

  public Module buildClientModule() {
    return new ControllableInjectionClientModule(controlledKeyToStrategyMap);
  }
  
  public Module buildServerModule(final Module... modules) {
    return new InterceptingBindingsBuilder()
      .install(modules)
      .install(new ProvisionInterceptorModule())
      .install(new ControllableInjectionServerModule(controlledKeyToStrategyMap))
      .intercept(controlledKeyToStrategyMap.keySet())
      .build();
  }

  public Module buildServerModule(final Collection<? extends Module> modules) {
    return new InterceptingBindingsBuilder()
      .install(modules)
      .install(new ProvisionInterceptorModule())
      .install(new ControllableInjectionServerModule(controlledKeyToStrategyMap))
      .intercept(controlledKeyToStrategyMap.keySet())
      .build();
  }

  private static class ProvisionInterceptorModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ProvisionInterceptor.class).to(MyProvisionInterceptor.class);
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
}