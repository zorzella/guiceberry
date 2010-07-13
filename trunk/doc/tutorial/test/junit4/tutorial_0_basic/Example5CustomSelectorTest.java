package junit4.tutorial_0_basic;

import com.google.guiceberry.GuiceBerryEnvSelector;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestDescription;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;
import com.google.inject.Module;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;

public class Example5CustomSelectorTest {

  @Rule
  public final GuiceBerryRule guiceBerry = new GuiceBerryRule(new MyGuiceBerryEnvSelector());

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

  @Test
  public void testFake() throws Exception {
    Assert.assertEquals("fake", server.getName());
  }

  @Test
  public void testReal() throws Exception {
    Assert.assertEquals("real", server.getName());
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
