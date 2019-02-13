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
package com.google.guiceberry;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of the {@link TestScoped} annotation.
 * 
 * @see Scope 
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
@Singleton
public class TestScope implements Scope {

  /**
   * Class representing an opaque test scope state to pass to a different thread
   * to share a current test scope.
   *
   * @see #getInternalStateForCurrentThread() 
   */
  public static class InternalState {
    private final TestDescription testDescription;

    private InternalState(TestDescription testDescription) {
      this.testDescription = testDescription;
    }
  }

  private final GuiceBerryUniverse universe;

  private final ConcurrentMap<TestDescription, Map<Key<?>, Object>> testMap =
      new ConcurrentHashMap<TestDescription, Map <Key<?>, Object>>();

  TestScope(GuiceBerryUniverse universe) {
    this.universe = universe;
  }

  void finishScope(TestDescription testCase) {
    testMap.remove(testCase);
  }

  /**
   * Save internal state for a current thread to pass to another thread.
   *
   * @see #setInternalStateForCurrentThread
   */
  public InternalState getInternalStateForCurrentThread() {
    return new InternalState(getActualTestCase());
  }
  
  /**
   * When current thread was created outside of a test or in a different test
   * one need to reset current thread's parent thread to share test scope
   * with it.
   *
   * @see #getInternalStateForCurrentThread() 
   */
  public void setInternalStateForCurrentThread(InternalState state) {
    // There is intentionally no TestDescription#isFinished check, two parallel
    // tests may share the same thread pool and the same thread.
    universe.currentTestDescriptionThreadLocal.set(state.testDescription);
  }

  private TestDescription getActualTestCase() {
    TestDescription actualTestCase = universe.currentTestDescriptionThreadLocal.get();
    if (actualTestCase == null) {
      throw new IllegalStateException(
          "GuiceBerry can't find out what is the currently-running test. " +
          "There are a few reasons why this can happen, but a likely one " +
          "is that a GuiceBerry Injector is being asked to instantiate a " +
          "class in a thread not created by your test case.");
    }
    if (actualTestCase.isFinished()) {
      throw new IllegalStateException(
          "GuiceBerry can't provide test scoped instances outside of a " +
          "currently-running test. There are a few reasons why this can " +
          "happen, but a likely one is that several tests share a thread " +
          "pool. " + Thread.currentThread() + " was created by " +
          actualTestCase.getTestId() + ". Consider using " +
          "TestScope#setInternalStateForCurrentThread for threads in " +
          "a thread pool .");
    }
    return actualTestCase;
  }

  @SuppressWarnings("unchecked")
  public synchronized <T> Provider<T> scope(final Key<T> key, 
      final Provider<T> creator) {

    return new Provider<T>() {
      public T get() {

        TestDescription actualTestCase = getActualTestCase();
        Map<Key<?>, Object> keyToInstanceProvider = testMap.get(actualTestCase);
        if (keyToInstanceProvider == null) {
          testMap.putIfAbsent(
              actualTestCase, new ConcurrentHashMap<Key<?>, Object>());
          keyToInstanceProvider = testMap.get(actualTestCase);
        }
        Object o = keyToInstanceProvider.get(key);
        if (o != null) {
          return (T) o;
        }
        // double checked locking -- handle with extreme care!
        synchronized(keyToInstanceProvider) {
          o = keyToInstanceProvider.get(key);
          if (o == null) {
            o = creator.get();
            keyToInstanceProvider.put(key, o);
          }
          return (T) o;
        }
      }
    };
  }
}
