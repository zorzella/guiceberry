package junit3.tutorial_0_basic;

import com.google.common.testing.TearDown;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;

import junit.framework.TestCase;

public class Example0HelloWorldTest extends TestCase {

  private TearDown toTearDown;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    toTearDown = ManualTearDownGuiceBerry.setup(this, Env.class);
  }
  
  @Override
  protected void tearDown() throws Exception {
    toTearDown.tearDown();
    super.tearDown();
  }
  
  public void testNothing() throws Exception {
    assertTrue(true);
  }

  public static final class Env extends GuiceBerryModule {}
}
