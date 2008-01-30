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

import com.google.inject.Key;
import com.google.inject.name.Names;

import junit.framework.TestCase;

/**
 * @author zorzella
 */
public class InjectionControllerTest extends TestCase {

  private InjectionController injectionController = new InjectionController();

  public void testCantOverrideDouble() throws Exception {
    injectionController.set(String.class, "foo");
    assertEquals("foo", injectionController.get(String.class));
    try {
      injectionController.set(String.class, "bar");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testKeyInjection() {
    Key<String> stringNamedTen = Key.get(String.class, Names.named("ten"));
    injectionController.set(stringNamedTen, "10");
    assertNull(injectionController.get(String.class));
    assertEquals("10", injectionController.get(stringNamedTen));
  }
}
