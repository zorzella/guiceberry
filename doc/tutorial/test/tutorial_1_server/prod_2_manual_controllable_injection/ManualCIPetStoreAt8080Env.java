package tutorial_1_server.prod_2_manual_controllable_injection;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.PortNumber;

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
  MyPetStoreServer startServer() {
    MyPetStoreServer result = new MyPetStoreServer(8080);
    // It's always sane to separate the "start"ing of a server from the
    // constructor.
    result.start();
    return result;
  }
  
  @Override
  protected Class<? extends TestScopeListener> getTestScopeListener() {
    return NoOpTestScopeListener.class;
  }
}