package com.google.inject.testing.guiceberry.tutorial;

import com.google.common.testing.TearDown;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryEnvRemapper;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.inject.testing.guiceberry.junit3.util.SimpleMapGuiceBerryEnvRemapper;

import junit.framework.TestCase;

@GuiceBerryEnv("com.google.inject.testing.guiceberry.tutorial.Example5Remapper$FakeGuiceBerryEnv")
public class Example5Remapper extends GuiceBerryJunit3TestCase {
  
  /**
   * The version of the Server injected depends on which GuiceBerryEnv is
   * used. If the {@link FakeGuiceBerryEnv} is used, this will be the "fake". If
   * the {@link RealGuiceBerryEnv} is used, this will be the "real".
   * 
   * <p>By default, {@link FakeGuiceBerryEnv} is used, since it's what shows up 
   * in the @GuiceBerryEnv class annotation above, ...
   */
  @Inject
  private Server server;

  @Override
  protected void setUp() throws Exception {
    /* 
     * ... but when we run the "testReal" test, we set a system property that
     * tells GuiceBerry to install a remapper.
     */
    if (this.getName() == "testReal") {
      clearSystemPropertyOnTearDown();
      System.setProperty(
          GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME,
          AnythingToRealRemapper.class.getName());
    }
    /*
     * Note this has to happen before we call GuiceBerryJunit3.setUp(this), so
     * it can't be done inside the test method!
     */
    super.setUp();
  }

  /**
   * This remapper simply tells GuiceBerry to always use the RealGuiceBerryEnv.
   * 
   * <p>More interesting remappers are more common. In fact, the most common 
   * pattern is to extend from {@link SimpleMapGuiceBerryEnvRemapper}. 
   */
  public static final class AnythingToRealRemapper implements GuiceBerryEnvRemapper {

    public String remap(TestCase testCase, String guiceBerryEnvName) {
      return RealGuiceBerryEnv.class.getName();
    }
  }

  public void testFake() throws Exception {
    assertEquals("fake", server.getName());
  }

  public void testReal() throws Exception {
    assertEquals("real", server.getName());
  }

  /**
   * This test makes sure to clear the system property on tearDown.
   */
  private void clearSystemPropertyOnTearDown() {
    TearDown tearDown = new TearDown() {
      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addTearDown(tearDown);
  }
  
  public static final class FakeGuiceBerryEnv extends GuiceBerryJunit3Env {
    @Override
    protected Class<? extends TestScopeListener> getTestScopeListener() {
      return NoOpTestScopeListener.class;
    }
    
    @Override
    protected void configure() {
      super.configure();
      bind(Server.class).to(FakeServer.class);
    }
  }

  public static final class RealGuiceBerryEnv extends GuiceBerryJunit3Env {
    @Override
    protected Class<? extends TestScopeListener> getTestScopeListener() {
      return NoOpTestScopeListener.class;
    }
    
    @Override
    protected void configure() {
      super.configure();
      bind(Server.class).to(RealServer.class);
    }
  }

  private interface Server {
    String getName();
  }
  
  private static final class RealServer implements Server {
    public String getName() {
      return "real";
    }
  }
  
  private static final class FakeServer implements Server {
    public String getName() {
      return "fake";
    }
  }
}
