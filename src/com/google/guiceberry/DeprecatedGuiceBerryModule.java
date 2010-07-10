// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.guiceberry;

import com.google.inject.Provides;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.TestScoped;

import junit.framework.TestCase;

/**
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

}
