package com.google.inject.testing.guiceberry.tutorial;

import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

@GuiceBerryEnv("com.google.inject.testing.guiceberry.tutorial.Example0HelloWorldTest$ExampleGuiceBerryEnv")
public class Example0HelloWorldTest extends GuiceBerryJunit3TestCase {

  public void testNothing() throws Exception {
    assertTrue(true);
  }

  public static final class ExampleGuiceBerryEnv extends GuiceBerryJunit3Env {
    @Override
    protected Class<? extends TestScopeListener> getTestScopeListener() {
      return NoOpTestScopeListener.class;
    }
  }
}
