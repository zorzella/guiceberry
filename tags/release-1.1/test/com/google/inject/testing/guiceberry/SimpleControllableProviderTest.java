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

import static com.google.inject.testing.guiceberry.SimpleControllableProvider.bindControllable;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

public class SimpleControllableProviderTest extends TearDownTestCase {
  interface Foo {}
  static class RealFoo implements Foo {}
  static class StubFoo implements Foo {}

  private Injector injector(final TestId login) {
    return Guice.createInjector(new AbstractModule() {

      @Override
      protected void configure() {
        bind(TestId.class).toInstance(login);
        bind(InjectionController.class).toProvider(InjectionControllerProvider.class);
        bindControllable(binder(), Foo.class)
            .to(RealFoo.class);
      }
    });
  }

  public void testMocking() {
    TestId login = new TestId("test1", "test1");
    InjectionControllerProvider
      .forTest(login, this)
      .set(Foo.class, new StubFoo());
    
    Foo foo = injector(login).getInstance(Foo.class);
    assertEquals(StubFoo.class, foo.getClass());
  }
  
  public void testNotMocking() {
    TestId testId = new TestId("test2", "test2");
    Foo foo = injector(testId).getInstance(Foo.class);
    assertEquals(RealFoo.class, foo.getClass());
  }

  public void testControlledProviderGetsInjected() {
    final TestId testId = new TestId("test", "test");

    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override protected void configure() {
        bind(TestId.class).toInstance(testId);
        bind(InjectionController.class).toProvider(InjectionControllerProvider.class);
        bindControllable(binder(), Boolean.class).toProvider(new Provider<Boolean>() {
          boolean initialized = false;
          @Inject void initialize() {
            initialized = true;
          }
          public Boolean get() {
            return initialized;
          }
        });
      }
    });

    assertTrue("Controlled provider should be injected",
        injector.getInstance(Boolean.class));
  }
}
