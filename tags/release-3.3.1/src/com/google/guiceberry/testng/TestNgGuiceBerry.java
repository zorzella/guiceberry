/*
 * Copyright (C) 2010 Google Inc.
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
package com.google.guiceberry.testng;

import com.google.common.testing.TearDown;
import com.google.guiceberry.DefaultEnvSelector;
import com.google.guiceberry.GuiceBerry;
import com.google.guiceberry.GuiceBerry.GuiceBerryWrapper;
import com.google.guiceberry.GuiceBerryEnvSelector;
import com.google.guiceberry.TestDescription;
import com.google.inject.Module;

import java.lang.reflect.Method;

/**
 * {@link GuiceBerry} adapter for TestNG tests.
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public class TestNgGuiceBerry {

  /**
   * Calls {@link #setUp(Object, Method, GuiceBerryEnvSelector)} passing a
   * {@link DefaultEnvSelector#of} the given {@code envClass}.
   *
   * <p>A canonical test will call this method in the an @BeforeMethod, passing
   * {@code this} as the testCase argument and the method received as an arg
   * to that @BeforeMethod. It will then store the return value to run it in
   * an @AfterMethod. See
   * {@link testng.tutorial_0_basic.Example0HelloWorldTest#setUp(Method)}.
   * @param method 
   */
  public static TearDown setUp(
      Object testCase, 
      Method method,
      Class<? extends Module> envClass) {
    return setUp(testCase, method, DefaultEnvSelector.of(envClass));
  }
  
  /**
   * Sets up the {@code testCase} with the given {@code guiceBerryEnvSelector}
   * and returns a {@link TearDown} whose {@link TearDown#tearDown()} method
   * must be called.
   */
  public static TearDown setUp(
      Object testCase,
      Method method,
      GuiceBerryEnvSelector guiceBerryEnvSelector) {
    final GuiceBerryWrapper setUpAndTearDown =
      GuiceBerry.INSTANCE.buildWrapper(buildTestDescription(testCase, method.getName()), 
          guiceBerryEnvSelector);
    setUpAndTearDown.runBeforeTest();
    return new TearDown() {
      
      public void tearDown() throws Exception {
        setUpAndTearDown.runAfterTest();
      }
    };
  }

  static TestDescription buildTestDescription(Object testCase, String methodName) {
    String testCaseName = testCase.getClass().getName();
    return new TestDescription(testCase, testCaseName + "." + methodName);
  }
}
