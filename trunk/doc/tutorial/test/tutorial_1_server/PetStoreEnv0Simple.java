package tutorial_1_server;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.testing.guiceberry.GuiceBerryEnvMain;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod.MyPetStoreServer;
import tutorial_1_server.prod.PortNumber;

public class PetStoreEnv0Simple extends GuiceBerryJunit3Env {
  
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
    return new MyPetStoreServer(8080);
  }
  
  @Override
  protected void configure() {
    super.configure();
    bind(GuiceBerryEnvMain.class).to(PetStoreServerStarter.class);
  }
  
  private static final class PetStoreServerStarter implements GuiceBerryEnvMain {
    
    @Inject
    private MyPetStoreServer myPetStoreServer;
    
    public void run() {
    // Starting a server should never be done in a @Provides (or Provider) method.
      myPetStoreServer.start();
    }
  }
}