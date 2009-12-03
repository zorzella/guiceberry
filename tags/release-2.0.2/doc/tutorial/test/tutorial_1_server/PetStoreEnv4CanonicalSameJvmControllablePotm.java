package tutorial_1_server;

import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.controllable.IcMaster;
import com.google.inject.testing.guiceberry.controllable.SharedStaticVarIcStrategy;
import com.google.inject.testing.guiceberry.controllable.TestIdServerModule;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod.MyPetStoreServer;
import tutorial_1_server.prod.PetOfTheMonth;
import tutorial_1_server.prod.PortNumber;

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
      protected Module getApplicationModule() {
        // !!! HERE !!!
        return icMaster.buildServerModule(
            new TestIdServerModule(),
            super.getApplicationModule());
      }
    };
    result.start();
    return result;
  }
  
  private IcMaster icMaster;
  
  @Override
  protected void configure() {
    super.configure();
    // !!!! HERE !!!!
    icMaster = new IcMaster()
      .thatControls(SharedStaticVarIcStrategy.strategy(),
         PetOfTheMonth.class);
    install(icMaster.buildClientModule());
  }
}

