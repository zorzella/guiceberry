package guiceberry2_junit3.tutorial_0_basic;

public interface Tutorial0Envs {

  String PACKAGE = "tutorial_0_basic.";
  String SUFFIX = "$Env";
  
  String EXAMPLE_1 = PACKAGE + "Example1GetsInjectedTest" + SUFFIX;

  String EXAMPLE_2 = PACKAGE + "Example2ScopesTest" + SUFFIX;
  
  String EXAMPLE_3 = PACKAGE + "Example3TestScopeListenerTest" + SUFFIX;
  
  String EXAMPLE_4 = PACKAGE + "Example4VerboseHelloWorldTest" + SUFFIX;

  String EXAMPLE_5 = PACKAGE + "Example5UseTearDownAccepterTest" + SUFFIX;
}
