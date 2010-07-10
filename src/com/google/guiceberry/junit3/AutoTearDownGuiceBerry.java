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
package com.google.guiceberry.junit3;

import com.google.common.testing.TearDown;
import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.DefaultEnvChooser;
import com.google.guiceberry.EnvChooser;
import com.google.guiceberry.GuiceBerry;
import com.google.guiceberry.GuiceBerry.GuiceBerryWrapper;
import com.google.guiceberry.TestDescription;
import com.google.guiceberry.TestId;
import com.google.inject.Module;

import junit.framework.TestCase;

/**
 * {@link GuiceBerry} adapter for JUnit3 {@link TearDownTestCase}s.
 *
 * @see ManualTearDownGuiceBerry
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public class AutoTearDownGuiceBerry {

  public static void setup(TearDownTestCase testCase, Class<? extends Module> envClass) {
    setup(testCase, DefaultEnvChooser.of(envClass));
  }
  
  public static void setup(TearDownTestCase testCase, EnvChooser envChooser) {
    final GuiceBerryWrapper toTearDown = 
      GuiceBerry.INSTANCE.buildWrapper(buildTestDescription(testCase, testCase.getName()), envChooser);
    testCase.addTearDown(new TearDown() {
      
      public void tearDown() throws Exception {
        toTearDown.runAfterTest();
      }
    })  ;
    toTearDown.runBeforeTest();
  }

  private static TestDescription buildTestDescription(TestCase testCase, String methodName) {
    String testCaseName = testCase.getClass().getName();
    return new TestDescription(testCase, testCaseName + "." + methodName,
      new TestId(testCaseName, methodName));
  }
}
