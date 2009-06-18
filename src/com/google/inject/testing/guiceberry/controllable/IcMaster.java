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

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;

import java.util.Collection;
import java.util.Map;

/**
 * As documented at length in the tutorial for 
 * {@link tutorial_1_server.Example4CanonicalSameJvmControllableInjectionTest}
 * etc, Controllable Injections need to be set up through an {@link IcMaster}, 
 * which is to participate both in the building of the test Injector as well as
 * the server Injector.
 * 
 * <p>Both Injectors need to basically agree on two different things: the list
 * of classes/keys that are subject to being controlled and, for each of these
 * classes/keys the {@link IcStrategy} that is to be used.
 * 
 * <p>Then, the client module (built through {@link #buildClientModule()} is to 
 * be added to the test Injector's list of modules; and, on the server side, the
 * {@link #buildServerModule(Collection)} is to be used.
 * 
 * @author Luiz-Otavio Zorzella
 * @author Jesse Wilson
 */
public final class IcMaster {
  
  private final Map<Key<?>, IcStrategy> controlledKeyToStrategyMap = 
    Maps.newHashMap();
  
  /**
   * Declares the given (un-annotated) {@code classes} as being subject to 
   * Controllable Injection with the given {@code strategy}.
   * 
   * @see #thatControls(IcStrategy, Key...) 
   *  
   * @return itself, for method chaining
   */
  public IcMaster thatControls(IcStrategy strategy, 
      Class<?>... classes) {
    for (Class<?> clazz : classes) {
      Key<?> key = Key.get(clazz);
      if (controlledKeyToStrategyMap.containsKey(key)) {
        throw new IllegalArgumentException(String.format(
            "The Key '%s' has already been declared as controlled. " +
            "Remove the duplicate declaration.", key));
      }
      controlledKeyToStrategyMap.put(key, strategy);
    }
    return this;
  }

  /**
   * Declares the given annotated classes (through they {@code keys}) as being 
   * subject to Controllable Injection with the given {@code strategy}.
   * 
   * @see #thatControls(IcStrategy, Class...)
   * 
   * @return itself, for method chaining
   */
  public IcMaster thatControls(IcStrategy strategy, 
      Key<?>... keys) {
    for (Key<?> key : keys) {
      if (controlledKeyToStrategyMap.containsKey(key)) {
        throw new IllegalArgumentException(String.format(
            "The Key '%s' has already been declared as controlled. " +
            "Remove the duplicate declaration.", key));
      }
      controlledKeyToStrategyMap.put(key, strategy);
    }
    return this;
  }

  /**
   * Use the {@link Module} returned from this method when constructing your
   * test {@link Injector}.
   */
  public Module buildClientModule() {
    return new ControllableInjectionClientModule(controlledKeyToStrategyMap);
  }

  /**
   * Use the {@link Module} returned from this method <em>instead</em> of the
   * {@code modules} passes as argument to create your server {@link Injector}
   * so it honors the declared Controllable Injections.
   * 
   * <p>The returned {@link Module} is equivalent to the given {@code modules},
   * except that the bindings to the classes/keys declared to be subject to 
   * be controlled (through {@link #thatControls(IcStrategy, Class...)}) will
   * be rewritten to honor this. 
   */
  public Module buildServerModule(final Module... modules) {
    return new InterceptingBindingsBuilder()
      .install(modules)
      .install(new ProvisionInterceptorModule())
      .install(new ControllableInjectionServerModule(controlledKeyToStrategyMap))
      .intercept(controlledKeyToStrategyMap.keySet())
      .build();
  }

  /**
   * @see #buildServerModule(Module...)
   */
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
        IcServer<T> instance = (IcServer<T>) injector.getInstance(IcStrategy.wrap(IcServer.class, key));
        return instance.getOverride(delegate);
      }
    }
  }
}