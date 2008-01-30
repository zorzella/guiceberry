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

import com.google.common.collect.Maps;
import com.google.inject.Key;

import java.util.Map;

/**
 * An {@link InjectionController} allows for tests to tinker with GUICE 
 * Injections.
 * 
 * @see InjectionControllerProvider
 * 
 * @author zorzella@google.com
 */
public class InjectionController {

  private Map<Key<?>,Object> map = Maps.newHashMap();

  /**
   * Returns the instance associated with a given class. This method is to be
   * called by any {@link com.google.inject.Provider} that provides something
   * you want to allow your tests to control.
   * 
   * <p>For the general case, instead of using this directly, just use a 
   * {@link SimpleControllableProvider}.
   */
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type) {
    return get(Key.get(type));
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Key<T> key) {
    return (T) map.get(key);
  }


  /**
   * <em>Never</em> call this method from production code, only from tests.
   * 
   * <p>setting a class into the {@link InjectionController} will allow for 
   * controllable providers (such as {@link SimpleControllableProvider}) to 
   * alter their injection.
   */
  public <T> InjectionController set(Key<T> key, T instance) {
    if (map.put(key, instance) != null) {
      throw new IllegalArgumentException(String.format(
          "Key '%s' was already being doubled.", key));
    }
    return this;
  }
  
  public <T> InjectionController set(Class<T> clazz, T instance) {
    return set(Key.get(clazz), instance);
  }

  //TODO: think about it -- this only exists for testing
  int size() {
    return map.size();
  }
}
