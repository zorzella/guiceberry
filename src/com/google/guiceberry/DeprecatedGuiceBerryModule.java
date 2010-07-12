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

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.inject.Provides;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.TestScoped;

import junit.framework.TestCase;

/**
 * Don't use this class -- it will go away. You've been warned.
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
@Deprecated
public class DeprecatedGuiceBerryModule extends GuiceBerryModule {

  public DeprecatedGuiceBerryModule() {
    super();
  }
  
  public DeprecatedGuiceBerryModule(GuiceBerryUniverse universe) {
    super(universe);
  }

  @Override
  protected void configure() {
    super.configure();
    bindScope(com.google.inject.testing.guiceberry.TestScoped.class, testScope);
  }
  
  @Provides
  @TestScoped
  TestCase getTestCase() {
    return (TestCase) universe.currentTestDescriptionThreadLocal.get().getTestCase();
  }

  @Provides
  @TestScoped
  TestId getDeprecatedTestId() {
    return universe.currentTestDescriptionThreadLocal.get().getTestId().toDeprecatedTestId();
  }

  @Deprecated
  public static void maybeAddGuiceBerryTearDown(
      ThreadLocal<TearDown> scaffoldingThreadLocal, final TestDescription testDescription,
      final TearDown toTearDown) {
    Object testToTearDown = testDescription.getTestCase();
    if (testToTearDown instanceof TearDownAccepter) {
      TearDownAccepter tdtc = (TearDownAccepter) testToTearDown;
      tdtc.addTearDown(toTearDown);
    } else {
      scaffoldingThreadLocal.set(toTearDown);
    }
  }

  
}
