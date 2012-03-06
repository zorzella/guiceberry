package junit3.tutorial_0_basic;

import com.google.common.testing.TearDown;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;
import com.google.inject.AbstractModule;

import junit.framework.TestCase;

public class Example0HelloWorldTest extends TestCase {

  private TearDown toTearDown;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Make this the call to ManualTearDownGuiceBerry.setUp as early as possible
    toTearDown = ManualTearDownGuiceBerry.setUp(this, Env.class);
  }
  
  @Override
  protected void tearDown() throws Exception {
    // Make this the call to ManualTearDownGuiceBerry.tearDown as late as possible
    toTearDown.tearDown();
    super.tearDown();
  }
  
  public void testNothing() throws Exception {
    assertTrue(true);
  }

  public static final class Env extends AbstractModule {
    @Override
    protected void configure() {
      install(new GuiceBerryModule());
    }
  }
}
