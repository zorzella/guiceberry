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
class TestScope implements Scope {

  private final GuiceBerryUniverse universe;

  private final ConcurrentMap<TestDescription, Map<Key<?>, Object>> testMap =
      new ConcurrentHashMap<TestDescription, Map <Key<?>, Object>>();

  TestScope(GuiceBerryUniverse universe) {
    this.universe = universe;
  }

  void finishScope(TestDescription testCase) {
    testMap.remove(testCase);
  }
  
  @SuppressWarnings("unchecked")  
  public synchronized <T> Provider<T> scope(final Key<T> key, 
      final Provider<T> creator) {

    return new Provider<T>() {
      public T get() {

        TestDescription actualTestCase = universe.currentTestDescriptionThreadLocal.get();
        if (actualTestCase == null) {
          throw new IllegalStateException(
              "GuiceBerry can't find out what is the currently-running test. " +
              "There are a few reasons why this can happen, but a likely one " +
              "is that a GuiceBerry Injector is being asked to instantiate a " +
              "class in a thread not created by your test case.");
        }
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
