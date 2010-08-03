package testng.tutorial_0_basic;

import com.google.common.testing.TearDown;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.testng.TestNgGuiceBerry;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public class Example0HelloWorldTest {

  private TearDown toTearDown;
  
  @BeforeMethod
  public void setUp(Method m) {
    // Make this the call to TestNgGuiceBerry.setUp as early as possible
    toTearDown = TestNgGuiceBerry.setUp(this, m, Env.class);
  }
  
  @AfterMethod
  public void tearDown() throws Exception {
    // Make this the call to TestNgGuiceBerry.tearDown as late as possible
    toTearDown.tearDown();
  }

  @Test
  public void testNothing() throws Exception {
    Assert.assertTrue(true);
  }

  public static final class Env extends GuiceBerryModule {}
}

