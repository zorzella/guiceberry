package guiceberry2_junit3.tutorial_1_server;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.testing.guiceberry.GuiceBerryEnvMain;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

import guiceberry2_junit3.tutorial_1_server.prod.MyPetStoreServer;
import guiceberry2_junit3.tutorial_1_server.prod.PetOfTheMonth;
import guiceberry2_junit3.tutorial_1_server.prod.PortNumber;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;


public final class PetStoreEnv2GlobalStaticControllablePotm extends GuiceBerryJunit3Env {
  
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
    MyPetStoreServer result = new MyPetStoreServer(8080) {
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