package junit3_tdtc.tutorial_0_basic;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;

public class Example0HelloWorldTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AutoTearDownGuiceBerry.setup(this, Env.class);
  }

  public void testNothing() throws Exception {
    assertTrue(true);
  }

  public static final class Env extends GuiceBerryModule {}
}
