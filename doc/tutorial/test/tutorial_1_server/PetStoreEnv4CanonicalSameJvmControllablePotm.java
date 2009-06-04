package tutorial_1_server;

import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod.MyPetStoreServer;
import tutorial_1_server.prod.PortNumber;

import java.util.List;

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
  MyPetStoreServer startServer() {
    MyPetStoreServer result = new MyPetStoreServer(8080) {
      @Override
      protected List<? extends Module> getModules() {
        return rewrite(super.getModules());
      }

    };
    result.start();
    return result;
  }

  // class LocalJvmControllableProvider<T> implements Provider<T> {}
  
  private static List<? extends Module> rewrite(List<? extends Module> modules) {
    // return new ModuleRewriter()
    //   .rewrite(PetOfTheMonth.class, LocalJvmControllableProdider.class);
    //   .build();
    return modules;
  }
}