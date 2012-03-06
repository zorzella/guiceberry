package tutorial_1_server.testing;

import com.google.guiceberry.GuiceBerryEnvMain;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestScoped;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod.PetStoreServer;

public class PetStoreEnv0Simple extends AbstractModule {
  
  @Provides @Singleton
  @PortNumber int getPortNumber() {
    return FreePortFinder.findFreePort();
  }
  
  @Provides @TestScoped
  WebDriver getWebDriver() {
    WebDriver driver = new HtmlUnitDriver();
    return driver;
  }
  
  @Provides
  @Singleton
  protected PetStoreServer buildPetStoreServer(@PortNumber int portNumber) {
    return new PetStoreServer(portNumber);
  }
  
  @Override
  protected void configure() {
    install(new GuiceBerryModule());
    bind(GuiceBerryEnvMain.class).to(PetStoreServerStarter.class);
  }
  
  private static final class PetStoreServerStarter implements GuiceBerryEnvMain {
    
    @Inject
    private PetStoreServer petStoreServer;
    
    public void run() {
      // Starting a server should never be done in a @Provides method 
      // (or inside Provider's get).
      petStoreServer.start();
    }
  }
}