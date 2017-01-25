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
package com.google.guiceberry;

import com.google.common.annotations.VisibleForTesting;
import com.google.guiceberry.GuiceBerryUniverse.TestCaseScaffolding;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.guiceberry.testng.TestNgGuiceBerry;

/**
 * You won't have to deal with this class directly unless you are writing a test
 * framework adapter.
 *
 * <p>Instead, use one of these:
 * 
 * <ul>
 *   <li>For Junit4, {@link GuiceBerryRule}
 *   <li>For plain Junit3 {@link ManualTearDownGuiceBerry}
 *   <li>For Junit3 with TearDownTestCase, {@link AutoTearDownGuiceBerry}
 *   <li>For TestNG, {@link TestNgGuiceBerry}
 * </ul>
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
@VisibleForTesting
public class GuiceBerry {

  public static final GuiceBerry INSTANCE = new GuiceBerry(GuiceBerryUniverse.INSTANCE);
  
  private final GuiceBerryUniverse universe;
  
  @VisibleForTesting
  GuiceBerry(GuiceBerryUniverse universe) {
    this.universe = universe;
  }

  /**
   * @see GuiceBerryWrapper
   */
  public GuiceBerryWrapper buildWrapper(TestDescription testDescription, GuiceBerryEnvSelector guiceBerryEnvSelector) {
    return new TestCaseScaffolding(testDescription, guiceBerryEnvSelector, universe);
  }
  
  /**
   * You won't need to deal with this interface unless you are writing an
   * adapter to a test framework. See {@link GuiceBerry}.
   * 
   * <p>The two methods in this interface should "wrap" a test execution. I.e.
   * before the test's execution, the method {@link #runBeforeTest()} should be
   * invoked; then, after the test's execution, the method
   * {@link #runAfterTest()} should be invoked.
   */
  public interface GuiceBerryWrapper {
    
    /**
     * @see GuiceBerryWrapper
     */
    void runBeforeTest();

    /**
     * @see GuiceBerryWrapper
     */
    void runAfterTest();
  }
}
