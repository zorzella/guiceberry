package junit4.tutorial_0_basic;

import static org.junit.Assert.assertEquals;

import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;

import org.junit.Rule;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Example1GetsInjectedTest {

  @Rule
  public final GuiceBerryRule guiceBerry = new GuiceBerryRule(Env.class);

  @Inject
  @NumberOneHundred
  private int number;

  @Test
  public void testHello() throws Exception {
    assertEquals(100, number);
  }

  public static final class Env extends AbstractModule {
    @Override
    protected void configure() {
      install(new GuiceBerryModule());
      bind(Integer.class).annotatedWith(NumberOneHundred.class).toInstance(100);
    }
  }

  @Retention(RetentionPolicy.RUNTIME) 
  @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) 
  @BindingAnnotation
  private @interface NumberOneHundred {}
}
