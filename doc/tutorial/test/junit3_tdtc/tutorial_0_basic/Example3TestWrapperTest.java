package junit3_tdtc.tutorial_0_basic;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestId;
import com.google.guiceberry.TestWrapper;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;

public class Example3TestWrapperTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AutoTearDownGuiceBerry.setUp(this, Env.class);
  }

  public void testOne() throws Exception {
    System.out.println("Inside testOne");
  }

  public void testTwo() throws Exception {
    System.out.println("Inside testTwo");
  }

  public static final class Example3TestWrapper implements TestWrapper {

    @Inject
    private Provider<TestId> testId;

    public void toRunBeforeTest() {
      System.out.println("Beginning: " + testId.get());
    }

    public void toRunAfterTest() {
      System.out.println("Ending: " + testId.get());
    }
  }

  public static final class Env extends GuiceBerryModule {
    
    @Override
    protected void configure() {
      super.configure();
      bind(TestWrapper.class).to(Example3TestWrapper.class).in(Scopes.SINGLETON);
    }
  }
}
