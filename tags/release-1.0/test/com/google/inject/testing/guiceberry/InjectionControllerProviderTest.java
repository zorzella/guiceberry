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

import com.google.common.testing.TearDownStack;
import com.google.inject.Provider;

import junit.framework.TestCase;

public class InjectionControllerProviderTest extends TestCase {

  private static class Foo {
    
    private final int bar;
 
    public Foo(int bar) {
      this.bar = bar;
    }
    
    @Override
    public boolean equals(Object that) {
      if (that.getClass() != getClass()) {
        return false;
      }
      return ((Foo)that).bar == bar;
    }
  }

  /**
   * Returns a {@link com.google.inject.Provider} that returns the object
   * passed in on a {@code get()} method call.
   *
   * <p> A provider of any supertype of T can be returned by upcasting
   * the {@code provided} argument.  For example:
   * <code>
   *   Provider&lt;Object&gt; objectProvider
   *      = Providers.providerFor((Object) "a string");
   * </code>
   */
  private static <T> Provider<T> providerFor(final T provided) {
    return new Provider<T>() {
      public T get() {
        return provided;
      }
    };
  }

  public void testSimpleSunnyCaseSemantics() throws Exception {
    TestId test1Id = new TestId("test1","test1");

    Provider<TestId> testIdProvider = providerFor(test1Id);

    InjectionControllerProvider injectionControllerProvider = 
      new InjectionControllerProvider(testIdProvider);
    // The InjectionControllerProvider starts empty
    assertEquals(0, injectionControllerProvider.size());

    InjectionController injectionController = injectionControllerProvider.get();
    // InjectionControllerProvider should never return a null
    assertNotNull(injectionController);

    // no one has "set" anything to the InjectionController
    assertEquals(0, injectionController.size());

    Foo foo = injectionController.get(Foo.class);
    // in particular, Foo.class is not in the controller
    assertNull(foo);
    // Nothing we did changed the InjectionControllerProvider so far
    assertEquals(0, injectionControllerProvider.size());


    TearDownStack tearDownStack = new TearDownStack();

    // At this point, we're changing the provider
    injectionController = InjectionControllerProvider.forTest(test1Id,
        tearDownStack);
    // The InjectionControllerProvider now has an entry
    assertEquals(1, injectionControllerProvider.size());

    injectionController = injectionControllerProvider.get();
    // InjectionControllerProvider should still never return a null
    assertNotNull(injectionController);

    // no one has yet "set" anything to the InjectionController
    assertEquals(0, injectionController.size());

    Foo expectedFoo = new Foo(10);
    injectionController.set(Foo.class, expectedFoo);

    // no one has yet "put" anything into InjectionController
    assertEquals(1, injectionController.size());

    foo = injectionController.get(Foo.class);

    // in particular, Foo.class is not in the controller
    assertEquals(expectedFoo, foo);

    tearDownStack.runTearDown(false);

    // After tear down, it should be empty again
    assertEquals(0, injectionControllerProvider.size());
  }
}
