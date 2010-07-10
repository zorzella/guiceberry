package junit3_tdtc.tutorial_0_basic;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
  
  public static Test suite() {
    TestSuite suite = new TestSuite(
      "Tests for junit3_tdtc.tutorial_0_basic");
    //$JUnit-BEGIN$
    suite.addTestSuite(Example0HelloWorldTest.class);
    suite.addTestSuite(Example1GetsInjectedTest.class);
    suite.addTestSuite(Example2ScopesTest.class);
    suite.addTestSuite(Example3TestScopeListenerTest.class);
    //$JUnit-END$
    return suite;
  }  
}
