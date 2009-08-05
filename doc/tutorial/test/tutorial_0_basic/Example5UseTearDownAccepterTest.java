package tutorial_0_basic;

import static com.google.inject.Scopes.SINGLETON;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.BasicJunit3Module;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3;

import junit.framework.TestCase;

@GuiceBerryEnv(Tutorial0Envs.EXAMPLE_5)
public class Example5UseTearDownAccepterTest extends TestCase {

  @Inject
  private TearDownAccepter tearDownAccepter;
  
  private int firstItem;
  private int secondItem;
  private boolean throwExceptionOnSecondItemDeleter = false;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    GuiceBerryJunit3.setUp(this);
    tearDownAccepter.addTearDown(new FirstItemResetter());
    firstItem = 1;

    tearDownAccepter.addTearDown(new SecondItemResetter());
    secondItem = 2;
  }
  
  @Override
  protected void tearDown() throws Exception {
    GuiceBerryJunit3.tearDown(this);
    
    // After GuiceBerryJunit3.tearDown, both "tearDowns" will have been executed
    assertEquals(0, firstItem);
    assertEquals(0, secondItem);
    super.tearDown();
  }

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
        throw new Exception("Let's say something went wrong here.");
      } else {
        secondItem = 0;
      }
    }
  }

  public static final class Env implements Module {

    public void configure(Binder binder) {
      binder.install(new BasicJunit3Module());
      binder.bind(TestScopeListener.class)
        .to(NoOpTestScopeListener.class)
        .in(SINGLETON);
    }
  }
}
