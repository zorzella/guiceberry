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
package com.google.guiceberry.junit4;

import com.google.guiceberry.DefaultEnvSelector;
import com.google.guiceberry.GuiceBerry;
import com.google.guiceberry.GuiceBerry.GuiceBerryWrapper;
import com.google.guiceberry.GuiceBerryEnvSelector;
import com.google.guiceberry.TestDescription;
import com.google.inject.Module;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * {@link GuiceBerry} adapter for JUnit4 tests, implementing {@MethodRule}.
 *
 * <p>This is the preferred implementation to use, as it does not depend on the client code
 * supplying the right object for {@code Object target}.
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public class GuiceBerryRule implements MethodRule {

  private GuiceBerryEnvSelector guiceBerryEnvSelector;

  public GuiceBerryRule(Class<? extends Module> envClass) {
    this(DefaultEnvSelector.of(envClass));
  }

  public GuiceBerryRule(GuiceBerryEnvSelector guiceBerryEnvSelector) {
    this.guiceBerryEnvSelector = guiceBerryEnvSelector;
  }

  public Statement apply(Statement base, FrameworkMethod method, Object target) {
    return apply(base, guiceBerryEnvSelector,
        new TestDescription(target,target.getClass().getName() + "." + method.getName()));
  }

  static Statement apply(
      final Statement base,
      final GuiceBerryEnvSelector guiceBerryEnvSelector,
      final TestDescription testDescription) {
    return new Statement() {
  
      @Override
      public void evaluate() throws Throwable {
        final GuiceBerryWrapper setupAndTearDown =
          GuiceBerry.INSTANCE.buildWrapper(
              testDescription, guiceBerryEnvSelector);
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
