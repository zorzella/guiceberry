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

package com.google.inject.testing.guiceberry.util;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class to create a {@link Scope} for mutable/replaceable 
 * singletons, i.e. those classes that, at any given moment in time, are 
 * singletons, but this singleton instance might be replaced.
 * 
 * @see #clear()
 * 
 * @author zorzella
 */
public class MutableSingletonScope implements Scope {

  private final Map<Key<?>, Object> backingMap = new HashMap<Key<?>, Object>();

  /**
   * Clears all the cached instances. After this method is called, any 
   * "singleton" bound to this scope that had already been created will be
   * created again next time it gets injected.
   */
  public void clear() {
    backingMap.clear();
  }
  
  @SuppressWarnings("unchecked")  
  public synchronized <T> Provider<T> scope(
      final Key<T> key, final Provider<T> unscoped) {

    return new Provider<T>() {
      public T get() {

        Object o = backingMap.get(key);

        if (o == null) {
          o = unscoped.get();
          backingMap.put(key, o);
        }
        return (T) o;
      }
    };
  }
}
  
