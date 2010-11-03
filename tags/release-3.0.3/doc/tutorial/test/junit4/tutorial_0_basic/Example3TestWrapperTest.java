package junit4.tutorial_0_basic;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Provides;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestId;
import com.google.guiceberry.TestWrapper;

import org.junit.Rule;
import org.junit.Test;

public class Example3TestWrapperTest {

  @Rule
  public final GuiceBerryRule guiceBerry = new GuiceBerryRule(Env.class);

  @Test
  public void testOne() throws Exception {
    System.out.println("Inside testOne");
  }

  @Test
  public void testTwo() throws Exception {
    System.out.println("Inside testTwo");
  }

  public static final class Env extends GuiceBerryModule {
    
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
