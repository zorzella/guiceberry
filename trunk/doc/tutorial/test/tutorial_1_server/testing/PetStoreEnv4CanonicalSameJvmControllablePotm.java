package tutorial_1_server.testing;

import com.google.guiceberry.GuiceBerryEnvMain;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestId;
import com.google.guiceberry.controllable.IcMaster;
import com.google.guiceberry.controllable.SharedStaticVarIcStrategy;
import com.google.guiceberry.controllable.TestIdServerModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod.MyPetStoreServer;
import tutorial_1_server.prod.Pet;
import tutorial_1_server.prod.PetOfTheMonth;

public final class PetStoreEnv4CanonicalSameJvmControllablePotm extends GuiceBerryModule {
  
  @Provides
  @PortNumber
  int getPortNumber(MyPetStoreServer server) {
    return server.getPortNumber();
  }
  
  @Provides
  WebDriver getWebDriver(@PortNumber int portNumber, TestId testId) {
    WebDriver driver = new HtmlUnitDriver();
    driver.get("http://localhost:" + portNumber);
    driver.manage().addCookie(
        new Cookie(TestId.COOKIE_NAME, testId.toString()));
    return driver;
  }
  
  @Provides
  @Singleton
  MyPetStoreServer buildPetStoreServer() {
    MyPetStoreServer result = new MyPetStoreServer() {
      @Override
      protected Module getPetStoreModule() {
        // !!! HERE !!!
        return icMaster.buildServerModule(
            new TestIdServerModule(),
            super.getPetStoreModule());
      }
    };
    return result;
  }
  
  private IcMaster icMaster;
  
  @Override
  protected void configure() {
    super.configure();
    bind(GuiceBerryEnvMain.class).to(PetStoreServerStarter.class);
    // !!!! HERE !!!!
    icMaster = new IcMaster()
      .thatControls(SharedStaticVarIcStrategy.strategy(),
         Key.get(Pet.class, PetOfTheMonth.class));
    install(icMaster.buildClientModule());
  }
  
  private static final class PetStoreServerStarter implements GuiceBerryEnvMain {
    
    @Inject
    private MyPetStoreServer myPetStoreServer;
    
    public void run() {
      // Starting a server should never be done in a @Provides method 
      // (or inside Provider's get).
      myPetStoreServer.start();
    }
  }
}

