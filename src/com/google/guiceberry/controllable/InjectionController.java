/*
 * Copyright (C) 2009 Google Inc.
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
package com.google.guiceberry.controllable;

/**
 * This is the most visible class of the Controllable Injections framework, and
 * should be injected into any TestCase or test infrastructure where you want
 * to control an injection.
 * 
 * <p>For example, say that {@code Foo} is a controlled injection. This is
 * how you can control it from a test:
 * 
 * <pre>
 *   {@code @}Inject
 *   IcClient&lt;Foo&gt; fooIcClient;
 *   
 *   public void testSpecialPath() {
 *     Foo someFoo = buildFooForThisTest();
 *     fooIcClient.setOverride(someFoo);
 *   }
 * </pre>
 * 
 * <p>Note that, to control an annotated binding, simply annotate the injection
 * of IcClient with the appropriate {@link com.google.inject.BindingAnnotation}.
 * 
 * @author Luiz-Otavio Zorzella
 * @author Jesse Wilson
 * 
 * @param <T> the type of the class you want to control.
 */
public interface InjectionController<T> {
  
  /**
   * Overrides the current test's injection of {@code T} to the {@code override}
   * value.
   * 
   * <p>Setting an override here will cause the appropriate implementation of
   * {@link IcStrategy.ClientSupport#setOverride(ControllableId, Object)}
   * to be called.
   * 
   * <p>Note that an override will clean itself up upon teardown. See 
   * {@link IcStrategy.ClientSupport#resetOverride(ControllableId)}.
   * 
   * <p>The usage of Controllable Injections is documented at length in the <a 
   * href="http://www.google.com/codesearch/p?hl=en#yL0uRk1mhCY/trunk/doc/tutorial/test/testng/tutorial_1_server/Example4InjectionControllerTest.java">
   * Example4InjectionControllerTest.java tutorial</a> and others.
   * 
   * <p>It is OK for a test to call this method multiple times.
   */
  void setOverride(T override);
  
  /**
   * "Undoes" all calls to {@link #setOverride(Object)}.
   * 
   * <p>The framework will always reset the overrides upon tearing down the 
   * test, so you do not need to. This is only provided in case you need to 
   * do so to complete the test altogether.
   * 
   * <p>It is OK for a test to call this method multiple times, even when
   * the injection is not currently being controlled, in which case it will
   * be a no-op.
   */
  void resetOverride();
}