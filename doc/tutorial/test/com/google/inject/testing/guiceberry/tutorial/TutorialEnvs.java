package com.google.inject.testing.guiceberry.tutorial;

public interface TutorialEnvs {

  String PACKAGE = "com.google.inject.testing.guiceberry.tutorial.";
  String SUFFIX = "$Env";
  
  String EXAMPLE_1 = PACKAGE + "Example1GetsInjectedTest" + SUFFIX;

  String EXAMPLE_2 = PACKAGE + "Example2ScopesTest" + SUFFIX;
  
  String EXAMPLE_3 = PACKAGE + "Example3TestScopeListenerTest" + SUFFIX;
  
  String EXAMPLE_4 = PACKAGE + "Example4VerboseHelloWorldTest" + SUFFIX;
}
