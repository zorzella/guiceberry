package tutorial_1_server.testing;

import com.google.common.collect.Maps;
import com.google.guiceberry.GuiceBerryEnvMain;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.TestId;
import com.google.guiceberry.TestScoped;
import com.google.guiceberry.controllable.TestIdServerModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod.PetStoreServer;
import tutorial_1_server.prod.Pet;

import java.util.Map;

public final class PetStoreEnv3CookiesOverride extends GuiceBerryModule {
  
  @Provides @Singleton
  @PortNumber int getPortNumber() {
    return FreePortFinder.findFreePort();
  }
  
  @Provides @TestScoped
  WebDriver getWebDriver(@PortNumber int portNumber, TestId testId) {
    WebDriver driver = new HtmlUnitDriver();
    // !!! HERE !!!
    driver.get("http://localhost:" + portNumber);
    driver.manage().addCookie(new Cookie(TestId.COOKIE_NAME, testId.toString()));
    return driver;
  }

  @Provides
  @Singleton
  PetStoreServer buildPetStoreServer(final PetStoreServerStarter starter, 
      @PortNumber int portNumber) {
    PetStoreServer result = new PetStoreServer(portNumber) {
      @Override
      protected Module getPetStoreModule() {
        return new PetStoreModuleWithTestIdBasedOverride(starter);
      }
    };
    return result;
  }
  
  @Override
  protected void configure() {
    super.configure();
    bind(GuiceBerryEnvMain.class).to(PetStoreServerStarter.class);
    bind(PetStoreServerStarter.class).in(Scopes.SINGLETON);
  }
  
  private static final class PetStoreServerStarter implements GuiceBerryEnvMain {

    @Inject
    private Provider<PetStoreServer> petStoreServer;
    
    Injector serverInjector;
    
    public void run() {
      // Starting a server should never be done in a @Provides method 
      // (or inside Provider's get).
      serverInjector = petStoreServer.get().start();
    }
  }

  public static final class PetStoreModuleWithTestIdBasedOverride 
      extends PetStoreServer.PetStoreModule {

    public static final Map<TestId, Pet> override = Maps.newHashMap();

    private PetStoreServerStarter starter;
    
    public PetStoreModuleWithTestIdBasedOverride(PetStoreServerStarter starter) {
      this.starter = starter;
    }

    // !!!HERE!!!!
    @Override
    protected Pet getFeaturedPet() {
      TestId testId = starter.serverInjector.getInstance(TestId.class);
      Pet featuredPetOverride = override.get(testId);
      if (featuredPetOverride != null) {
        return featuredPetOverride;
      }
      return calculateFeaturedPet();
    }

    @Override
    protected void configure() {
      super.configure();
      install(new TestIdServerModule());
    }
  }
}