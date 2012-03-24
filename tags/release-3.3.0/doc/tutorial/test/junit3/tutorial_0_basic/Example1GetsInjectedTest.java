package junit3.tutorial_0_basic;

import com.google.common.testing.TearDown;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;

import junit.framework.TestCase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Example1GetsInjectedTest extends TestCase {

  private TearDown toTearDown;
  
  @Override
  protected void tearDown() throws Exception {
    toTearDown.tearDown();
    super.tearDown();
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    toTearDown = ManualTearDownGuiceBerry.setUp(this, Env.class);
  }

  @Inject
  @NumberOneHundred
  private int number;

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
