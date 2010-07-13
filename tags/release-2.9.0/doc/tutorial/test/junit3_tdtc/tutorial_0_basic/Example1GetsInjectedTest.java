package junit3_tdtc.tutorial_0_basic;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Example1GetsInjectedTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AutoTearDownGuiceBerry.setUp(this, Env.class);
  }

  @Inject
  @NumberOneHundred
  private int number;

  public void testHello() throws Exception {
    assertEquals(100, number);
  }

  public static final class Env extends GuiceBerryModule {
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
