package junit3.tutorial_0_basic;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;
import com.google.inject.Inject;

import junit.framework.TestCase;

public class Example4UseTearDownAccepterTest extends TestCase {

  private TearDown toTearDown;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    toTearDown = ManualTearDownGuiceBerry.setUp(this, Env.class);

    tearDownAccepter.addTearDown(new FirstItemResetter());
    firstItem = 1;

    tearDownAccepter.addTearDown(new SecondItemResetter());
    secondItem = 2;
  }
  
  @Override
  protected void tearDown() throws Exception {
    toTearDown.tearDown();
    super.tearDown();
  }
  
  @Inject
  private TearDownAccepter tearDownAccepter;
  
  private int firstItem;
  private int secondItem;
  private boolean throwExceptionOnSecondItemDeleter = false;

  public void testOne() throws Exception {
    assertEquals(1, firstItem);
    assertEquals(2, secondItem);
  }

  public void testTwoFailsWithException() throws Exception {
    assertEquals(1, firstItem);
    throwExceptionOnSecondItemDeleter = true;
    assertEquals(2, secondItem);
  }

  private class FirstItemResetter implements TearDown {
    public void tearDown() {
      System.out.println("first item delete");
      firstItem = 0;
    }
  }

  private class SecondItemResetter implements TearDown {
    public void tearDown() throws Exception {
      System.out.println("second item delete");
      // This assertion passes because TearDownAccepter guarantees to call 
      // tearDowns in a reverse order. Since setUp adds SecondItemResetter after
      // FirstItemResetter, the first item still has not been cleared.
      assertEquals(1, firstItem);
   
      if (throwExceptionOnSecondItemDeleter) {
        // We'll simulate a situation where a tear down goes wrong -- like getting
        // an exception trying to close a File or DB connection or whatever.
        throw new Exception("This test is expected to fail! Let's say something went wrong here.");
      } else {
        secondItem = 0;
      }
    }
  }

  public static final class Env extends GuiceBerryModule {}
}
