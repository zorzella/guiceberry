package tutorial_1_server.testing;

import com.google.guiceberry.GuiceBerryEnvMain;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestScoped;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod.PetStoreServer;
import tutorial_1_server.prod.Pet;

public final class PetStoreEnv2StaticOverride extends GuiceBerryModule {
  
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
  PetStoreServer buildPetStoreServer(@PortNumber int portNumber) {
    PetStoreServer result = new PetStoreServer(portNumber) {
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
    private PetStoreServer myPetStoreServer;
    
    public void run() {
      // Starting a server should never be done in a @Provides method 
      // (or inside Provider's get).
      myPetStoreServer.start();
    }
  }

  public static final class PetStoreModuleWithGlobalStaticOverride 
      extends PetStoreServer.PetStoreModule {

    // !!!HERE!!!!
    public static Pet override;
    
    @Override
    protected Pet calculateFeaturedPet() {
      // !!!HERE!!!!
      if (override != null) {
        return override;
      }
      return super.calculateFeaturedPet();
    }
  }
}