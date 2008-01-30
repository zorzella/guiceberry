// Copyright 2008 Google Inc. All rights reserved.

package com.google.inject.testing.guiceberry;

import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunitTest;
import com.google.inject.testing.guiceberry.junit3.MutableSingletonScopeTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite =
        new TestSuite("Test for com.google.inject.testing.guiceberry.junit3");
    //$JUnit-BEGIN$
    suite.addTestSuite(MutableSingletonScopeTest.class);
    suite.addTestSuite(GuiceBerryJunitTest.class);
    //$JUnit-END$
    return suite;
  }

}
