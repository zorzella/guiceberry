/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.inject.testing.guiceberry;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;

import java.lang.annotation.Annotation;
import java.util.UUID;

/**
 * Boilerplate-killer for common case when all one needs in terms of 
 * controlling the injection is the ability to install a double 
 * (fake/mock/stub/dummy). 
 * 
 * <p>E.g. (see {@link SimpleControllableProviderTest}), say one has the 
 * binding {@code bind(Foo.class).to(RealFoo.class)}, but wants to allow for 
 * tests to use the {@link InjectionControllerProvider} to install a 
 * {@code StubFoo.class} instead. The binding becomes:
 * 
 * {@code bind(Foo.class).toProvider(SimpleControllableProvider.create(Foo.class, RealFoo.class))}
 * 
 * @author zorzella
 *
 * @param <T>
 */
public class SimpleControllableProvider<T> implements Provider<T> {
  
  @Inject private Provider<InjectionController> injectionControllerProvider;
  @Inject private Injector injector;
  private final Key<T> key;
  private final Key<? extends T> implementationKey;

  private SimpleControllableProvider(
      Key<T> key,
      Key<? extends T> implementationKey) {
    this.key = key;
    this.implementationKey = implementationKey;
  }

  public T get() {
    Preconditions.checkNotNull(injector, "injector");
    T mockT = injectionControllerProvider.get().get(key);
    return (mockT == null)
        // This always happens in production
        ? injector.getInstance(implementationKey)
        // This will happen when running tests that "control" a <T>'s injection
        : mockT;
  }

  /**
   * Bind the specified class for Controllable Injection.
   */
  public static <T> LinkedBindingBuilder<T> bindControllable(Binder binder, Class<T> type) {
    return bindControllable(binder, Key.get(type));
  }

  /**
   * Bind the specified key for Controllable Injection.
   */
  public static <T> LinkedBindingBuilder<T> bindControllable(Binder binder, Key<T> key) {
    // we need two bindings to use regular Providers with
    // Controllable Providers. We create a private named binding for the
    // Provider, and then use ControllableInject to control that.
    Annotation uniqueAnnotation = Names.named("private binding " + UUID.randomUUID());
    Key<T> delegateKey = Key.get(key.getTypeLiteral(), uniqueAnnotation);
    binder.bind(key).toProvider(new SimpleControllableProvider<T>(key, delegateKey));
    return binder.bind(delegateKey);
  }
}
