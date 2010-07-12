package junit4.tutorial_1_server;

import com.google.guiceberry.GuiceBerryEnvMain;
import com.google.guiceberry.GuiceBerryModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import junit4.tutorial_1_server.PetStoreEnv2GlobalStaticControllablePotm.PetStoreModuleWithGlobalStaticOverride;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod.MyPetStoreServer;
import tutorial_1_server.prod.PetOfTheMonth;
import tutorial_1_server.prod.PortNumber;

public final class PetStoreEnv2GlobalStaticControllablePotm extends GuiceBerryModule {
  
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
  MyPetStoreServer buildPetStoreServer() {
    MyPetStoreServer result = new MyPetStoreServer() {
      @Override
      protected Module getPetStoreModule() {
        return new PetStoreModuleWithGlobalStaticOverride();
      }
    };
    return result;
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
      // Starting a server should never be done in a @Provides method 
      // (or inside Provider's get).
      myPetStoreServer.start();
    }
  }

  public static final class PetStoreModuleWithGlobalStaticOverride 
      extends MyPetStoreServer.PetStoreModule {

    // !!!HERE!!!!
    public static PetOfTheMonth override;
    
    @Override
    protected PetOfTheMonth somePetOfTheMonth() {
      // !!!HERE!!!!
      if (override != null) {
        return override;
      }
      return super.somePetOfTheMonth();
    }
  }
}