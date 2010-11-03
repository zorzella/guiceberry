package junit4.tutorial_0_basic;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Example4UseTearDownAccepterTest {

  @Rule
  public final GuiceBerryRule guiceBerry = new GuiceBerryRule(Env.class);

  @Inject
  private TearDownAccepter tearDownAccepter;
  
  private int firstItem;
  private int secondItem;
  private boolean throwExceptionOnSecondItemDeleter = false;

  @Before
  public void setUp() throws Exception {
    tearDownAccepter.addTearDown(new FirstItemResetter());
    firstItem = 1;

    tearDownAccepter.addTearDown(new SecondItemResetter());
    secondItem = 2;
  }
  
  @After
  public void tearDown() throws Exception {
    // @Afters happen before the tear downs
    assertEquals(1, firstItem);
    assertEquals(2, secondItem);
  }

  @Test
  public void testOne() throws Exception {
    assertEquals(1, firstItem);
    assertEquals(2, secondItem);
  }

  @Test
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
