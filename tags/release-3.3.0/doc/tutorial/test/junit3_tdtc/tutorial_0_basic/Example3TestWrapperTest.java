package junit3_tdtc.tutorial_0_basic;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestId;
import com.google.guiceberry.TestWrapper;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

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

  public static final class Env extends AbstractModule {
    @Override
    protected void configure() {
      install(new GuiceBerryModule());
    }
    @Provides
    TestWrapper getTestWrapper(final TestId testId,
        final TearDownAccepter tearDownAccepter) {
      
      return new TestWrapper() {
        
        public void toRunBeforeTest() {
          tearDownAccepter.addTearDown(new TearDown() {
            
            public void tearDown() throws Exception {
              System.out.println("Ending: " + testId);
            }
          });
          System.out.println("Beginning: " + testId);
        }
      };
    }
  }
}
