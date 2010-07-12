package junit4.tutorial_0_basic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
Example0HelloWorldTest.class, 
Example1GetsInjectedTest.class, 
Example2ScopesTest.class, 
Example3TestWrapperTest.class, 
})
public class AllTests {}
