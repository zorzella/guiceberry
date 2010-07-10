package guiceberry2_junit3.tutorial_2_advanced;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
      "Tests for guiceberry2_junit3.tutorial_2_advanced");
    //$JUnit-BEGIN$
    suite.addTestSuite(Example5Remapper.class);
    //$JUnit-END$
    return suite;
  }
}
