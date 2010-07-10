package junit3_tdtc.tutorial_0_basic;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestId;
import com.google.guiceberry.TestScopeListener;
import com.google.guiceberry.junit3.TearDownGuiceBerry;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;

public class Example3TestScopeListenerTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    TearDownGuiceBerry.setup(this, Env.class);
  }

  public void testOne() throws Exception {
    System.out.println("Inside testOne");
  }

  public void testTwo() throws Exception {
    System.out.println("Inside testTwo");
  }

  public static final class Example3TestScopeListener implements TestScopeListener {

    @Inject
    private Provider<TestId> testId;

    public void enteringScope() {
      System.out.println("Entering scope of: " + testId.get());
    }

    public void exitingScope() {
      System.out.println("Exiting scope of: " + testId.get());
    }
  }

  public static final class Env extends GuiceBerryModule {
    
    @Override
    protected void configure() {
      // TODO Auto-generated method stub
      super.configure();
      bind(TestScopeListener.class).to(Example3TestScopeListener.class).in(Scopes.SINGLETON);
    }
  }
}
