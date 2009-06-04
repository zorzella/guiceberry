package tutorial_0_basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

@GuiceBerryEnv(Tutorial0Envs.EXAMPLE_1)
public class Example1GetsInjectedTest extends GuiceBerryJunit3TestCase {

  @Inject
  @NumberOneHundred
  private int number;

  public void testHello() throws Exception {
    assertEquals(100, number);
  }

  public static final class Env extends GuiceBerryJunit3Env {
    @Override
    protected Class<? extends TestScopeListener> getTestScopeListener() {
      return NoOpTestScopeListener.class;
    }

    @Override
    protected void configure() {
      super.configure();
      bind(Integer.class).annotatedWith(NumberOneHundred.class).toInstance(100);
    }
  }

  @Retention(RetentionPolicy.RUNTIME) 
  @Target(ElementType.FIELD) 
  @BindingAnnotation
  private @interface NumberOneHundred {
  }
}
