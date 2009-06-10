package tutorial_1_server;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.controllable.TestIdServerModule;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod.MyPetStoreServer;
import tutorial_1_server.prod.PetOfTheMonth;
import tutorial_1_server.prod.PortNumber;

import java.util.Map;
import java.util.Random;

public final class PetStoreEnv3CookiesControlledPotm extends GuiceBerryJunit3Env {
  
  @Provides
  @PortNumber
  int getPortNumber(MyPetStoreServer server) {
    return server.getPortNumber();
  }
  
  @Provides
  WebDriver getWebDriver(@PortNumber int portNumber, TestId testId) {
    WebDriver driver = new HtmlUnitDriver();
    // !!! HERE !!!
    driver.get("http://localhost:" + portNumber);
    driver.manage().addCookie(
        new Cookie(TestId.COOKIE_NAME, testId.toString()));
    return driver;
  }
  
  @Provides
  @Singleton
  MyPetStoreServer startServer() {
    MyPetStoreServer result = new MyPetStoreServer(8080) {
      @Override
      protected Module getApplicationModule() {
        return new AbstractModule() {
          @Override
          protected void configure() {
            install(new PetStoreModuleWithTestIdBasedOverride());
            install(new ServletModule());
            install(new TestIdServerModule());
          }
        };
      }
    };
    result.start();
    return result;
  }
  
  public static final class PetStoreModuleWithTestIdBasedOverride extends AbstractModule {

    public static final Map<TestId, PetOfTheMonth> override = Maps.newHashMap();

    @Provides
    // !!!HERE!!!!
    PetOfTheMonth getPetOfTheMonth(TestId testId) {
      PetOfTheMonth petOfTheMonth = override.get(testId);
      if (petOfTheMonth != null) {
        return petOfTheMonth;
      }
      return somePetOfTheMonth();
    }

    private final Random rand = new Random();

    /** Simulates a call to a non-deterministic service -- maybe an external
     * server, maybe a DB call to a volatile entry, etc.
     */
    private PetOfTheMonth somePetOfTheMonth() {
      PetOfTheMonth[] allPetsOfTheMonth = PetOfTheMonth.values();
      return allPetsOfTheMonth[(rand.nextInt(allPetsOfTheMonth.length))];
    }

    @Override
    protected void configure() {
      install(new ServletModule());
    }
  }
}