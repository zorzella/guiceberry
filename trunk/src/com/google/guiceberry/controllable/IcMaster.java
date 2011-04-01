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
 * As documented at length in the <a 
 * href="http://www.google.com/codesearch/p?hl=en#yL0uRk1mhCY/trunk/doc/tutorial/test/testng/tutorial_1_server/Example4InjectionControllerTest.java">
 * Example4InjectionControllerTest.java tutorial</a> and others,
 * Controllable Injections need to be set up through an {@link IcMaster}, 
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
   * Convenient wrapper to {@link #thatControls(IcStrategy, Key...)} for  
   * un-annotated {@code classes}.
   * 
   * <p>Note that these are totally equivalent statements:
   * 
   * <pre>
   *   thatControls(someStrategy(),
   *     MyClass.class)
   * </pre>
   * 
   * and
   * 
   * <pre>
   *   thatControls(someStrategy(),
   *     Key.get(MyClass.class))
   * </pre>
   * 
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
   * Declares the given annotated classes (through their {@code keys}) as being 
   * subject to Controllable Injection with the given {@code strategy}.
   * 
   * <p>For example, to control the statement 
   * {@code @Inject @MyAnnotation MyClass myClass;} use the following:
   * 
   * <pre>
   * IcMaster icMaster = new IcMaster()
   *   .thatControls(someStrategy(),
   *     Key.get(MyClass.class, MyAnnotation.class));
   * </pre>
   * 
   * <p>Passing the same key twice to the same {@link IcMaster} results in an 
   * {@link IllegalArgumentException}.
   * 
   * <p>To control generified classes, use the 
   * {@link com.google.inject.TypeLiteral} anonymous inner class trick. 
   * E.g. to control {@code @Inject MyClass<SomeType> myClass}}, use:
   * 
   * <pre>
   * IcMaster icMaster = new IcMaster()
   *   .thatControls(someStrategy(),
   *     Key.get(new TypeLiteral<MyClass<SomeType>>(){}));
   * </pre>
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