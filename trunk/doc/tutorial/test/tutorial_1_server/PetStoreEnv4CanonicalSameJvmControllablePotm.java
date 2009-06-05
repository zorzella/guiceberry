package tutorial_1_server;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.prod.MyPetStoreServer;
import tutorial_1_server.prod.PetOfTheMonth;
import tutorial_1_server.prod.PortNumber;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.internal.Maps;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;

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
    private final Map<Class<?>, ControllerSupport> map = Maps.newHashMap();
    
    // TODO Create instance
    MyClientProvider myClientProvider;
    
    // TODO Create instance
    MyServerProvider myServerProvider;

    private static final class MyClientProvider implements Provider<ClientController> {
     
      @Inject
      ModuleRewriter moduleRewriter;

      @Inject
      Provider<TestId> testIdProvider;

      @Inject
      Injector injector;
      
      Map<Class<?>, ClientControllerSupport> myMap = Maps.newHashMap();
      
      public ClientController get() {
        return new ClientController() {     
          public synchronized <T> void setOverride(Class<T> clazz, T override) {
            ClientControllerSupport clientControllerSupport = myMap.get(clazz);
            if (clientControllerSupport == null) {
              clientControllerSupport = injector.getInstance(moduleRewriter.map.get(clazz).clientControllerClass());
              myMap.put(clazz, clientControllerSupport);
            }
            clientControllerSupport.setOverride(new Pair(testIdProvider.get(), clazz), override);
          }
        };
      }      
    }
    
    private static final class MyServerProvider implements Provider<ServerController> {
      
      @Inject
      ModuleRewriter moduleRewriter;

      @Inject
      Provider<TestId> testIdProvider;
      
      @Inject
      Injector injector;

      Map<Class<?>, ServerControllerSupport> myMap = Maps.newHashMap();
      
      public ServerController get() {
        return new ServerController() {     
          public synchronized <T> T getOverride(Class<T> clazz) {
            ServerControllerSupport serverControllerSupport = myMap.get(clazz);
            if (serverControllerSupport == null) {
              serverControllerSupport = injector.getInstance(moduleRewriter.map.get(clazz).serverControllerClass());
              myMap.put(clazz, serverControllerSupport);
            }
            return serverControllerSupport.getOverride(new Pair(testIdProvider.get(), clazz));
          }
        };
      }      
    }

    public Provider<ClientController> getClientControllerProvider() {
      return myClientProvider;
    }
    
    public Provider<ServerController> getServerControllerProvider() {
      return myServerProvider;
    }
    
    
    
    public ModuleRewriter forModules(List<? extends Module> modules) {
      if (modules != null) {
        throw new IllegalStateException();
      }
      this.modules = modules;
      return this;
    }

    public ModuleRewriter rewrite(ControllerSupport factory
        , 
        Class<?>... clazzes
        ) {
    for (Class<?> claz : clazzes) {
      map.put(claz, factory);
    }
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
  
  public interface ServerController {  
    <T> T getOverride(Class<T> clazz);
  }
  
  public interface ClientController {
    <T> void setOverride(Class<T> clazz, T override);
  }

  public interface ServerControllerSupport {  
    <T> T getOverride(Pair pair);
  }
  
  public interface ClientControllerSupport {
    <T> void setOverride(Pair pair, T override);
  }
  
  public static class ControllerSupport {
    
    private final Class<? extends ClientControllerSupport> clientControllerSupport;
    private final Class<? extends ServerControllerSupport> serverControllerSupport;

    public ControllerSupport(
        Class<? extends ClientControllerSupport> clientControllerSupport,
        Class<? extends ServerControllerSupport> serverControllerSupport
        ) {
      this.clientControllerSupport = clientControllerSupport;
      this.serverControllerSupport = serverControllerSupport;
    }
    
    Class<? extends ClientControllerSupport> clientControllerClass() {
      return clientControllerSupport;
    }
    Class<? extends ServerControllerSupport> serverControllerClass() {
      return serverControllerSupport;
    }
  }
  
  public static class Pair {
    private final TestId testId;
    private final Class<?> clazz;

    public Pair(TestId test, Class<?> clazz) {
      this.testId = test;
      this.clazz = clazz;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
      result = prime * result + ((testId == null) ? 0 : testId.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Pair other = (Pair) obj;
      if (clazz == null) {
        if (other.clazz != null)
          return false;
      } else if (!clazz.equals(other.clazz))
        return false;
      if (testId == null) {
        if (other.testId != null)
          return false;
      } else if (!testId.equals(other.testId))
        return false;
      return true;
    }
    
  }
  
  static class LocalJvmControllableProvider extends ControllerSupport {

    private static final Map<Pair,Object> map = Maps.newHashMap();

    public LocalJvmControllableProvider() {
      super(MyClientController.class, MyServerController.class);
    }
    
    private static final class MyClientController implements ClientControllerSupport {

      public <T> void setOverride(Pair pair, T override) {
        map.put(pair, override);
      }
    }
    
    private static final class MyServerController implements ServerControllerSupport {
      public <T> T getOverride(Pair pair) {
        return (T) map.get(pair);
      }
    }
  }
  
  private static List<? extends Module> rewrite(
      ModuleRewriter moduleRewriter, 
      List<? extends Module> modules) {
     List<? extends Module> result = moduleRewriter
       .forModules(modules)
       .rewrite(new LocalJvmControllableProvider(),
           PetOfTheMonth.class)
       .build();
    return result;
  }
}

