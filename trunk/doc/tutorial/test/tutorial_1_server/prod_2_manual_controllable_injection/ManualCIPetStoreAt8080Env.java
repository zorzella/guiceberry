package tutorial_1_server.prod_2_manual_controllable_injection;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod_0_simple.MyPetStoreServer;
import tutorial_1_server.prod_0_simple.PetOfTheMonth;
import tutorial_1_server.prod_0_simple.PortNumber;

import java.util.List;
import java.util.Random;

public final class ManualCIPetStoreAt8080Env extends GuiceBerryJunit3Env {
  
  @Provides
  @PortNumber
  int getPortNumber(MyPetStoreServer server) {
    return server.getPortNumber();
  }
  
  @Provides
  WebDriver getWebDriver() {
    WebDriver driver = new HtmlUnitDriver();
    return driver;
  }

  @Provides
  @Singleton
  protected MyPetStoreServer startServer() {
    MyPetStoreServer result = new MyPetStoreServer(8080) {
      @Override
      protected List<? extends Module> getModules() {
        return Lists.newArrayList(
            new PetStoreModuleWithGlobalStaticOverride(),
            new ServletModule());
      }
    };
    result.start();
    return result;
  }
  
  public static final class PetStoreModuleWithGlobalStaticOverride extends AbstractModule {

    // !!!HERE!!!!
    public static PetOfTheMonth override;
    
    @Provides
    PetOfTheMonth getPetOfTheMonth() {
      // !!!HERE!!!!
      if (override != null) {
        return override;
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