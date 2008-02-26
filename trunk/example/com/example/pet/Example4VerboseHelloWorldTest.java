package com.example.pet;

import static com.google.inject.Scopes.SINGLETON;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.BasicJunit3Module;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3;

@GuiceBerryEnv("com.example.pet.Example4VerboseHelloWorldTest$ExampleGuiceBerryEnv")
public class Example4VerboseHelloWorldTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    GuiceBerryJunit3.setUp(this);
  }
  
  public void testOne() throws Exception {
    assertTrue(true);
  }

  public static final class ExampleGuiceBerryEnv implements Module {

    public void configure(Binder binder) {
      binder.install(new BasicJunit3Module());
      binder.bind(TestScopeListener.class)
        .to(NoOpTestScopeListener.class)
        .in(SINGLETON);
    }
  }
}
