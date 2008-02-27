package com.google.inject.testing.guiceberry.tutorial;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for com.google.inject.testing.guiceberry.tutorial");
		//$JUnit-BEGIN$
		suite.addTestSuite(Example1GetsInjectedTest.class);
		suite.addTestSuite(Example3TestScopeListenerTest.class);
		suite.addTestSuite(Example2ScopesTest.class);
		suite.addTestSuite(Example0HelloWorldTest.class);
		suite.addTestSuite(Example4VerboseHelloWorldTest.class);
		//$JUnit-END$
		return suite;
	}

}
