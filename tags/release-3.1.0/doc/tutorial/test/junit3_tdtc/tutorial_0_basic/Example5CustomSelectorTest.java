package junit3_tdtc.tutorial_0_basic;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.GuiceBerryEnvSelector;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestDescription;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.inject.Inject;
import com.google.inject.Module;

public class Example5CustomSelectorTest extends TearDownTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AutoTearDownGuiceBerry.setUp(this, new MyGuiceBerryEnvSelector());
  }

  /**
   * This selector returns FakeGuiceBerryEnv.class whenever the "testFake"
   * method is executed, and RealGuiceBerryEnv.class whenever the "testReal"
   * method is used.
   * 
   * <p>Therefore, even though the testFake and testReal methods are identical,
   * their server.getName()s return different values.
   *
   * <p>This is not necessarily a particularly useful usage of 
   * {@link GuiceBerryEnvSelector}s, but it makes for a nice compact tutorial
   * example.
   */
  private static final class MyGuiceBerryEnvSelector implements GuiceBerryEnvSelector {
    public Class<? extends Module> guiceBerryEnvToUse(TestDescription testDescription) {
      String name = testDescription.getName();
      if (name.endsWith("testFake")) {
        return FakeGuiceBerryEnv.class;
      } else if (name.endsWith("testReal")) {
        return RealGuiceBerryEnv.class;
      } else {
        throw new IllegalArgumentException();
      }
    }
  }
  
  /**
   * The version of the Server injected depends on which GuiceBerryEnv is
   * used. If the {@link FakeGuiceBerryEnv} is used, this will be the "fake". If
   * the {@link RealGuiceBerryEnv} is used, this will be the "real".
   */
  @Inject
  private Server server;

  public void testFake() throws Exception {
    assertEquals("fake", server.getName());
  }

  public void testReal() throws Exception {
    assertEquals("real", server.getName());
  }

  public static final class FakeGuiceBerryEnv extends GuiceBerryModule {
    @Override
    protected void configure() {
      super.configure();
      bind(Server.class).to(FakeServer.class);
    }
  }

  public static final class RealGuiceBerryEnv extends GuiceBerryModule {
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
