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

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.commands.intercepting.InterceptingInjectorBuilder;
import com.google.inject.name.Names;
import com.google.common.collect.Sets;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * @author zorzella
 * @author jessewilson@google.com (Jesse Wilson)
 */
public class InjectionControllerTest extends TestCase {

  private InjectionController injectionController = new InjectionController();

  public void testCantOverrideDouble() throws Exception {
    injectionController.addSubstitutableKeys(Sets.<Key<?>>immutableSet(Key.get(String.class)));
    injectionController.substitute(String.class, "foo");
    assertEquals("foo", injectionController.getSubstitute(String.class));
    try {
      injectionController.substitute(String.class, "bar");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }
  
  public void testKeyInjection() {
    injectionController.addSubstitutableKeys(Sets.<Key<?>>immutableSet(
        Key.get(String.class, Names.named("ten"))));
    Key<String> stringNamedTen = Key.get(String.class, Names.named("ten"));
    injectionController.substitute(stringNamedTen, "10");
    assertNull(injectionController.getSubstitute(String.class));
    assertEquals("10", injectionController.getSubstitute(stringNamedTen));
  }

  public void testSimpleOverride() throws Exception {
    Injector injector = new InterceptingInjectorBuilder()
        .install(injectionController.createModule(),
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(String.class).toInstance("a");
              }
            })
        .intercept(String.class)
        .build();

    assertEquals("a", injector.getInstance(String.class));
    injectionController.substitute(String.class, "b");
    assertEquals("b", injector.getInstance(String.class));
  }

  /**
   * TODO(jessewilson): this is suppressed until we can make the
   *    production injector create the InjectionController, rather
   *    than the static InjectionControllerProvider.forTest method.
   */
  public void SUPPRESSED_testSubstituteWithoutWhitelistFails() throws Exception {
    Injector injector = new InterceptingInjectorBuilder()
        .install(injectionController.createModule(),
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(String.class).toInstance("a");
              }
            })
        .build();

    try {
      injectionController.substitute(String.class, "b");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    assertEquals("a", injector.getInstance(String.class));
  }

  public void testBareBindingFails() throws Exception {
    InterceptingInjectorBuilder builder = new InterceptingInjectorBuilder()
        .install(injectionController.createModule(),
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(ArrayList.class);
              }
            })
        .intercept(ArrayList.class);

    try {
      builder.build();
      fail();
    } catch (UnsupportedOperationException expected) {
    }
  }
}
