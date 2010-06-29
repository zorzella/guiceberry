package tutorial_2_advanced;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
      "Test for com.google.inject.testing.guiceberry.tutorial_0_basic");
    //$JUnit-BEGIN$
    suite.addTestSuite(Example5Remapper.class);
    //$JUnit-END$
    return suite;
  }
}
