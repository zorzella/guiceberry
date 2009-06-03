package com.google.inject.testing.guiceberry.tutorial_0_basic;

import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

@GuiceBerryEnv("com.google.inject.testing.guiceberry.tutorial_0_basic.Example0HelloWorldTest$Env")
public class Example0HelloWorldTest extends GuiceBerryJunit3TestCase {

  public void testNothing() throws Exception {
    assertTrue(true);
  }

  public static final class Env extends GuiceBerryJunit3Env {
    @Override
    protected Class<? extends TestScopeListener> getTestScopeListener() {
      return NoOpTestScopeListener.class;
    }
  }
}
