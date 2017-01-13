/*
 * Copyright (C) 2017 Google Inc.
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
package com.google.guiceberry.junit4;

import com.google.guiceberry.GuiceBerry;
import com.google.guiceberry.GuiceBerry.GuiceBerryWrapper;
import com.google.guiceberry.GuiceBerryEnvSelector;
import com.google.guiceberry.TestDescription;
import org.junit.runners.model.Statement;

/**
 * Base class for Junit4 {@link GuiceBerry} adapters.
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
class GuiceBerryBaseRule {

  private final GuiceBerryEnvSelector guiceBerryEnvSelector;

  protected GuiceBerryBaseRule(GuiceBerryEnvSelector guiceBerryEnvSelector) {
    this.guiceBerryEnvSelector = guiceBerryEnvSelector;
  }

  public Statement apply(final Statement base, final TestDescription testDescription) {
    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        final GuiceBerryWrapper setupAndTearDown =
          GuiceBerry.INSTANCE.buildWrapper(testDescription, guiceBerryEnvSelector);
        try {
          setupAndTearDown.runBeforeTest();
          base.evaluate();
        } finally {
          setupAndTearDown.runAfterTest();
        }
      }
    };
  }
}
