package tutorial_1_server;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.internal.Maps;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod.MyPetStoreServer;
import tutorial_1_server.prod.PetOfTheMonth;
import tutorial_1_server.prod.PortNumber;

import java.util.List;
import java.util.Map;

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
  MyPetStoreServer startServer(final ModuleRewriter moduleRewriter) {
    MyPetStoreServer result = new MyPetStoreServer(8080) {
      @Override
      protected List<? extends Module> getModules() {
        return rewrite(moduleRewriter, super.getModules());
      }

    };
    result.start();
    return result;
  }

  public interface InjectionControllerRegistry {

    <T> InjectionControllerI<T> get(Class<T> clazz);
    <T> InjectionControllerI<T> get(Key<T> clazz);
  }
  
  public interface InjectionControllerI<T> {
    void substituteFor(T override);
  }
  
  public static final class ModuleRewriter {

    private List<? extends Module> modules;
    // Each entry in the map is of the same "?" as its key
    private final Map<Class<?>, Controller<?>> map = Maps.newHashMap();
    
    public InjectionControllerRegistry getInjectionController(final TestId testId) {
      return new InjectionControllerRegistry() {
        
        public <T> InjectionControllerI<T> get(final Class<T> clazz) {
          
          return new InjectionControllerI<T>() {

            public void substituteFor(T override) {
//              map.get(clazz).setOverride(testId, override);
            }
          };
        }

        public <T> InjectionControllerI<T> get(Key<T> clazz) {
          return null;
        }
        
      };
    }

    public ModuleRewriter forModules(List<? extends Module> modules) {
      if (modules != null) {
        throw new IllegalStateException();
      }
      this.modules = modules;
      return this;
    }

    public <T> ModuleRewriter rewrite(Class<T> clazz, 
        Controller<T> controller) {
      map.put(clazz, controller);
      return this;
    }

    public List<? extends Module> build() {
      return modules;
    }
  }
  
  @Override
  protected void configure() {
    super.configure();
    bind(ModuleRewriter.class).in(Scopes.SINGLETON);
  }
  
  @Provides
  InjectionControllerRegistry getInjectionController(TestId testId, ModuleRewriter moduleRewriter) {
    return moduleRewriter.getInjectionController(testId);
  }
  
  public interface Controller<T> {
    
    public T getOverride(TestId testId);
    public void setOverride(TestId testId, T override);
    
  }
  
  static class LocalJvmControllableProvider<T> implements Controller<T> {

    Map<TestId,T> map = Maps.newHashMap();
    
    public T getOverride(TestId testId) {
      return map.get(testId); 
    }

    public void setOverride(TestId testId, T override) {
      map.put(testId, override);
    }
  }
  
  private static List<? extends Module> rewrite(
      ModuleRewriter moduleRewriter, 
      List<? extends Module> modules) {
     List<? extends Module> result = moduleRewriter
       .forModules(modules)
       .rewrite(PetOfTheMonth.class, 
           new LocalJvmControllableProvider<PetOfTheMonth>())
       .build();
    return result;
  }
}