package junit3.tutorial_0_basic;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestId;
import com.google.guiceberry.TestWrapper;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import junit.framework.TestCase;

public class Example3TestWrapperTest extends TestCase {

  private TearDown toTearDown;
  
  @Override
  protected void tearDown() throws Exception {
    toTearDown.tearDown();
    super.tearDown();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    toTearDown = ManualTearDownGuiceBerry.setUp(this, Env.class);
  }

  public void testOne() throws Exception {
    System.out.println("Inside testOne");
  }

  public void testTwo() throws Exception {
    System.out.println("Inside testTwo");
  }

  public static final class Env extends GuiceBerryModule {
    
    @Provides
    @Singleton
    TestWrapper getTestWrapper(final Provider<TestId> testId,
        final TearDownAccepter tearDownAccepter) {
      
      return new TestWrapper() {
        
        public void toRunBeforeTest() {
          tearDownAccepter.addTearDown(new TearDown() {
            
            public void tearDown() throws Exception {
              System.out.println("Ending: " + testId.get());
            }
          });
          System.out.println("Beginning: " + testId.get());
        }
      };
    }
  }
}
