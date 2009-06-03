package com.google.inject.testing.guiceberry.tutorial_1_server;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
      "Test for com.google.inject.testing.guiceberry.tutorial_0_basic");
    //$JUnit-BEGIN$
    suite.addTestSuite(Example0HelloWorldTest.class);
    //$JUnit-END$
    return suite;
  }
}
