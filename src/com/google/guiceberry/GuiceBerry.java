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

import com.google.guiceberry.GuiceBerryUniverse.TestCaseScaffolding;

/**
 * @author Luiz-Otavio "Z" Zorzella
 */
public class GuiceBerry {

  public static TestCaseScaffolding setup(TestDescription testDescription, EnvChooser envChooser) {
    TestCaseScaffolding scaffolding = 
      GuiceBerryUniverse.INSTANCE.new TestCaseScaffolding(testDescription, envChooser);
    scaffolding.goSetUp();
    return scaffolding;
  }
}
