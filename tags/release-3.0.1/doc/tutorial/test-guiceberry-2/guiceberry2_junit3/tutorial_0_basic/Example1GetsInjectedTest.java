package guiceberry2_junit3.tutorial_0_basic;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
    protected void configure() {
      super.configure();
      bind(Integer.class).annotatedWith(NumberOneHundred.class).toInstance(100);
    }
  }

  @Retention(RetentionPolicy.RUNTIME) 
  @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) 
  @BindingAnnotation
  private @interface NumberOneHundred {}
}
