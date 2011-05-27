package guiceberry2_junit3.tutorial_1_server;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.testing.guiceberry.GuiceBerryEnvMain;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.controllable.IcMaster;
import com.google.inject.testing.guiceberry.controllable.SharedStaticVarIcStrategy;
import com.google.inject.testing.guiceberry.controllable.TestIdServerModule;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

import guiceberry2_junit3.tutorial_1_server.prod.MyPetStoreServer;
import guiceberry2_junit3.tutorial_1_server.prod.PetOfTheMonth;
import guiceberry2_junit3.tutorial_1_server.prod.PortNumber;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;


public final class PetStoreEnv4CanonicalSameJvmControllablePotm extends GuiceBerryJunit3Env {
  
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
    MyPetStoreServer result = new MyPetStoreServer(8080) {
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
         PetOfTheMonth.class);
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

