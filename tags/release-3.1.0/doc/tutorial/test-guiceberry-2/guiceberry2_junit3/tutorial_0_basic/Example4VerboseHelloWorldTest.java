package guiceberry2_junit3.tutorial_0_basic;

import static com.google.inject.Scopes.SINGLETON;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.BasicJunit3Module;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3;

import junit.framework.TestCase;

@GuiceBerryEnv(Tutorial0Envs.EXAMPLE_4)
public class Example4VerboseHelloWorldTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    GuiceBerryJunit3.setUp(this);
  }

  @Override
  protected void tearDown() throws Exception {
    GuiceBerryJunit3.tearDown(this);
    super.tearDown();
  }
  
  public void testOne() throws Exception {
    assertTrue(true);
  }

  public void testTwo() throws Exception {
    assertTrue(true);
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
