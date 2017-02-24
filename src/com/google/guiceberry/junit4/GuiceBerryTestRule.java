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

import com.google.guiceberry.DefaultEnvSelector;
import com.google.guiceberry.GuiceBerry;
import com.google.guiceberry.GuiceBerryEnvSelector;
import com.google.guiceberry.TestDescription;
import com.google.inject.Module;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link GuiceBerry} adapter for JUnit4 tests, implementing {@link TestRule}.
 *
 * @author David M. Hull
 */
public final class GuiceBerryTestRule implements TestRule {
  private final Object target;
  private final GuiceBerryEnvSelector guiceBerryEnvSelector;

  /**
   * Main constructor.
   *
   * @param target Always {@code this}.
   * @param guiceBerryEnvSelector Instance to use as env selector.
   */
  public GuiceBerryTestRule(Object target, GuiceBerryEnvSelector guiceBerryEnvSelector) {
    this.target = target;
    this.guiceBerryEnvSelector = guiceBerryEnvSelector;
  }

  /**
   * Convenience for the common case of a selector class with a no-arg constructor.
   *
   * @param target Always {@code this}.
   * @param envClass Class to use for env selector.  Must have a no-arg constructor.
   */
  public GuiceBerryTestRule (Object target, Class<? extends Module> envClass) {
    this(target, DefaultEnvSelector.of(envClass));
  }

  public Statement apply(Statement base, Description description) {
    if (!description.getTestClass().isInstance(target)) {
      throw new IllegalArgumentException("Rule target is not an instance of "
          + description.getTestClass().getName()
          + "\nAlways create a GuiceBerryTestRule with this (that is, the test object) "
          + "as the first constructor argument");
    }
    return GuiceBerryRule.apply(base, guiceBerryEnvSelector,
        new TestDescription(target,
            target.getClass().getName() + "." + description.getMethodName()));
  }
}
