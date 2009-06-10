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

package com.google.inject.testing.guiceberry.tokill;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.testing.guiceberry.TestId;

import java.util.Map;

/**
 * @deprecated this form of injection controller is no longer supported and will 
 * be killed in the future. Use the new 
 * {@link com.google.inject.testing.guiceberry.controllable} paradigm.
 *
 * This Guice Provider builds {@link InjectionController}s.
 *
 * <p>It also provides the static {@link #forTest(TestId, TearDownAccepter)}
 * which is the point of entry for tests to find an {@link InjectionController}
 * to tinker with.
 *
 * <p>E.g. say we want to be able to @Inject a double (mock/fake/stub) in place
 * of Foo. The general pattern is:
 *
 * <ol>
 *  <li>Have {@code Foo}'s binding use a {@code FooProvider}
 *  <li>{@code FooProvider} has an {@link InjectionController} @Injected into
 *    it. Note that {@link InjectionController} is constant for a single user
 *    (i.e. behaves like a user-scoped object}.
 *  <li>{@code FooProvider}'s "get" method looks at this
 *    {@link InjectionController} to see it has whatever it wants (it is simply
 *    a map of Class<T> to T, so anything can be put into it). E.g., it may look
 *    for an instance of Foo.class itself. If one is found (should only happen
 *    in tests, never in production), it return *it* rather than build one, as
 *    it would otherwise do.
 *  <li>The test case can (statically) get an instance of the
 *    {@link InjectionController} mapped to any username. It then sets anything
 *    to it -- again, generally, it may set a "Foo.class" to a mock/stub/fake
 *    instace of Foo.
 * </ol>
 *
 * TODO: Add an example
 *
 * @author Luiz-Otavio Zorzella
 */
@Deprecated
public class InjectionControllerProvider implements Provider<InjectionController> {

  private static final Map<TestId, InjectionController> MAP = Maps.newHashMap();

  private static final InjectionController BLANK = new InjectionController() {
    @Override public <T> InjectionController substitute(Key<T> key, T instance) {
      throw new UnsupportedOperationException(
      "It seems you are trying to 'set' some controller parameter, but you\n" +
      "have the 'BLANK' InjectionController for some reason. You either have\n" +
      "some piece of production code calling 'set' (which is wrong), or you " +
      "are somehow not getting the right InjectionController in your test.\n" +
      "\n" +
      "Check that your test is getting an InjectionController from the \n" +
      "forUser methods.\n");
    }
  };

  private final Provider<TestId> testIdProvider;

  @Inject
  public InjectionControllerProvider(
      Provider<TestId> testIdProvider){
    this.testIdProvider = testIdProvider;
  }

  public InjectionController get() {
    TestId testId = testIdProvider.get();
    // In production, TestId will always be null, and we just return a blank
    // singleton
    if (testId == null) {
      return BLANK;
    }
    synchronized (MAP) {
      InjectionController result = MAP.get(testId);
      // tests that do not want to override anything will also get BLANK
      if (result == null) {
        return BLANK;
      }
      return result;
    }
  }

  /**
   * <em>Never</em> call this method from production code, only from tests.
   *
   * <p>Returns the {@link InjectionController} linked to {@code userLogin}.
   *
   * <p>This overload of the method registers a tearDown to clean up itself
   * once the test is over.
   */
  public static InjectionController forTest(
      final TestId testId,
      TearDownAccepter tearDownAccepter) {
    // TODO(jessewilson): This code would benefit from Map.supplyIfAbsent
    Preconditions.checkNotNull(testId);
    InjectionController result;
    synchronized (MAP) {
      result = MAP.get(testId);

      if (result == null) {
        result = new InjectionController();
        MAP.put(testId, result);

        tearDownAccepter.addTearDown(new TearDown() {
          public void tearDown() throws Exception {
            synchronized(MAP) {
              MAP.remove(testId);
            }
          }
        });
      }
    }
    return result;
  }

  //TODO: think about it -- this only exists for testing
  int size() {
    synchronized(MAP) {
      return MAP.size();
    }
  }
}