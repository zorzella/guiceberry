package com.google.inject.testing.guiceberry.tutorial_0_basic;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

@GuiceBerryEnv(Tutorial0Envs.EXAMPLE_3)
public class Example3TestScopeListenerTest extends GuiceBerryJunit3TestCase {

  public void testOne() throws Exception {
    System.out.println("Inside testOne");
  }

  public void testTwo() throws Exception {
    System.out.println("Inside testTwo");
  }

  public static final class Example3TestScopeListener implements TestScopeListener {

    @Inject
    private Provider<TestId> testId;

    public void enteringScope() {
      System.out.println("Entering scope of: " + testId.get());
    }

    public void exitingScope() {
      System.out.println("Exiting scope of: " + testId.get());
    }
  }

  public static final class Env extends GuiceBerryJunit3Env {
    @Override
    protected Class<? extends TestScopeListener> getTestScopeListener() {
      return Example3TestScopeListener.class;
    }
  }
}
