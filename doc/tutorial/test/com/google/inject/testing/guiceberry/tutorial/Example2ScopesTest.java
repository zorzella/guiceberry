package com.google.inject.testing.guiceberry.tutorial;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.TestScoped;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

@GuiceBerryEnv(TutorialEnvs.EXAMPLE_2)
public class Example2ScopesTest extends GuiceBerryJunit3TestCase {

  @Inject
  @UnscopedIncrementingNumber
  private Provider<Integer> unscopedIncrementingNumber;

  @Inject
  @TestScopedIncrementingNumber
  private Provider<Integer> testScopedIncrementingNumber;

  @Inject
  @SingletonScopedIncrementingNumber
  private Provider<Integer> singletonScopedIncrementingNumber;

  public void testOne() throws Exception {
    assertEquals(300, singletonScopedIncrementingNumber.get().intValue());
    assertEquals(300, singletonScopedIncrementingNumber.get().intValue());

    assertEquals(200, testScopedIncrementingNumber.get().intValue());
    assertEquals(200, testScopedIncrementingNumber.get().intValue());

    assertEquals(100, unscopedIncrementingNumber.get().intValue());
    assertEquals(101, unscopedIncrementingNumber.get().intValue());
  }

  public void testTwo() throws Exception {
    assertEquals(300, singletonScopedIncrementingNumber.get().intValue());
    assertEquals(300, singletonScopedIncrementingNumber.get().intValue());

    assertEquals(201, testScopedIncrementingNumber.get().intValue());
    assertEquals(201, testScopedIncrementingNumber.get().intValue());

    assertEquals(102, unscopedIncrementingNumber.get().intValue());
    assertEquals(103, unscopedIncrementingNumber.get().intValue());
  }

  public static final class Env extends GuiceBerryJunit3Env {
    private static final class IncrementingProvider implements Provider<Integer> {
      private int number;

      public IncrementingProvider(int seed) {
        this.number = seed;
      }

      public Integer get() {
        return number++;
      }
    }

    @Override
    protected Class<? extends TestScopeListener> getTestScopeListener() {
      return NoOpTestScopeListener.class;
    }

    @Override
    protected void configure() {
      super.configure();
      IncrementingProvider unscopedIncrementingNumberProvider = 
        new IncrementingProvider(100);
      IncrementingProvider testScopedIncrementingNumberProvider = 
        new IncrementingProvider(200);
      IncrementingProvider singletonScopedIncrementingNumberProvider = 
        new IncrementingProvider(300);
      bind(Integer.class)
        .annotatedWith(UnscopedIncrementingNumber.class)
        .toProvider(unscopedIncrementingNumberProvider);
      bind(Integer.class)
        .annotatedWith(TestScopedIncrementingNumber.class)
        .toProvider(testScopedIncrementingNumberProvider)
        .in(TestScoped.class);
      bind(Integer.class)
        .annotatedWith(SingletonScopedIncrementingNumber.class)
        .toProvider(singletonScopedIncrementingNumberProvider)
        .in(Scopes.SINGLETON);
    }
  }

  @Retention(RetentionPolicy.RUNTIME) 
  @Target(ElementType.FIELD) 
  @BindingAnnotation
  private @interface UnscopedIncrementingNumber {}

  @Retention(RetentionPolicy.RUNTIME) 
  @Target(ElementType.FIELD) 
  @BindingAnnotation
  private @interface TestScopedIncrementingNumber {}

  @Retention(RetentionPolicy.RUNTIME) 
  @Target(ElementType.FIELD) 
  @BindingAnnotation
  private @interface SingletonScopedIncrementingNumber {}
}
