package tutorial_1_server;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.internal.Maps;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;
import com.google.inject.util.Types;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import tutorial_1_server.PetStoreEnv4CanonicalSameJvmControllablePotm.IcStrategyCouple.IcClientStrategy;
import tutorial_1_server.PetStoreEnv4CanonicalSameJvmControllablePotm.IcStrategyCouple.IcServerStrategy;
import tutorial_1_server.prod.MyPetStoreServer;
import tutorial_1_server.prod.PetOfTheMonth;
import tutorial_1_server.prod.PortNumber;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
  MyPetStoreServer startServer(final IcMaster moduleRewriter) {
    MyPetStoreServer result = new MyPetStoreServer(8080) {
      @Override
      protected List<? extends Module> getModules() {
        List<Module> modules = new ArrayList<Module>();
        // TODO: add rewritten modules instead
        modules.addAll(super.getModules());
        modules.add(moduleRewriter.buildServerModule());
        return modules;
      }
    };
    result.start();
    return result;
  }
  
  @Override
  protected void configure() {
    super.configure();
    // !!!! HERE !!!!
    createControllerBindings();
  }

  private void createControllerBindings() {
    IcMaster moduleRewriter = new IcMaster()
      .thatControls(new LocalJvmIcStrategy().getControllerSupport(),
         PetOfTheMonth.class);
    install(moduleRewriter.buildClientModule());
    bind(IcMaster.class).toInstance(moduleRewriter);
  }
  
  static final class ControllableInjectionClientModule extends AbstractModule {
    
    private final Map<Key<?>, IcStrategyCouple> rewriter;
    
    public ControllableInjectionClientModule(Map<Key<?>, IcStrategyCouple> rewriter) {
      this.rewriter = rewriter;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
      for (Map.Entry<Key<?>, IcStrategyCouple> e : rewriter.entrySet()) {
        bindWithAnnotation(binder(), Types.newParameterizedType(IcClient.class, e.getKey().getTypeLiteral().getType()), e.getKey())
           .toProvider(new MyClientProvider(e.getKey(), getProvider(TestId.class), 
               getProvider(e.getValue().clientControllerClass())));
      }
    }
  
    private static final class MyClientProvider<T> implements Provider<IcClient<T>> {
      private final Key<T> key;
      private final Provider<IcClientStrategy> clientControllerSupportProvider;
      private final Provider<TestId> testIdProvider;
      
      public MyClientProvider(Key<T> key,  
          Provider<TestId> testIdProvider, Provider<IcClientStrategy> clientControllerSupportProvider) {
        this.key = key;
        this.testIdProvider = testIdProvider;
        this.clientControllerSupportProvider = clientControllerSupportProvider;
      }

      public IcClient<T> get() {
        return new IcClient<T>() {     
          public void setOverride(T override) {
            clientControllerSupportProvider.get().setOverride(
                new ControllableId(testIdProvider.get(), key), override);
          }
        };
      }
    }
  }
  
  static AnnotatedBindingBuilder<?> bindWithAnnotation(Binder binder, Type type, Key<?> annotationHolder) {
    AnnotatedBindingBuilder<?> builder = binder.bind(TypeLiteral.get(type));
    if (annotationHolder.getAnnotation() != null) {
      builder.annotatedWith(annotationHolder.getAnnotation());
    } else if (annotationHolder.getAnnotationType() != null) {
      builder.annotatedWith(annotationHolder.getAnnotationType());
    }
    return builder;
  }

  static final class ControllableInjectionServerModule extends AbstractModule {
    
    private final Map<Key<?>, IcStrategyCouple> rewriter;
    
    public ControllableInjectionServerModule(Map<Key<?>, IcStrategyCouple> rewriter) {
      this.rewriter = rewriter;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
      for (Map.Entry<Key<?>, IcStrategyCouple> e : rewriter.entrySet()) {
        bindWithAnnotation(binder(), Types.newParameterizedType(IcServer.class, e.getKey().getTypeLiteral().getType()), e.getKey())
             .toProvider(new MyServerProvider(e.getKey(), getProvider(TestId.class), 
               getProvider(e.getValue().clientControllerClass())));
      }
    }

    private static final class MyServerProvider<T> implements Provider<IcServer<T>> {
      private final Key<T> key;
      private final Provider<IcServerStrategy> serControllerSupportProvider;
      private final Provider<TestId> testIdProvider;
      
      public MyServerProvider(Key<T> key,  
          Provider<TestId> testIdProvider, Provider<IcServerStrategy> serverControllerSupportProvider) {
        this.key = key;
        this.testIdProvider = testIdProvider;
        this.serControllerSupportProvider = serverControllerSupportProvider;
      }

      public IcServer<T> get() {
        return new IcServer<T>() {     
          public T getOverride() {
            return serControllerSupportProvider.get().getOverride(
                new ControllableId(testIdProvider.get(), key));
          }
        };
      }
    }
  }
  
  public static final class IcMaster {
    
    private final Map<Key<?>, IcStrategyCouple> controlledKeysToStrategy = Maps.newHashMap();
    
    public IcMaster thatControls(IcStrategyCouple support, 
        Class<?>... classes) {
      for (Class<?> clazz : classes) {
        Key<?> key = Key.get(clazz);
        if (controlledKeysToStrategy.containsKey(key)) {
          throw new IllegalArgumentException();
        }
        controlledKeysToStrategy.put(key, support);
      }
      return this;
    }

    public IcMaster thatControlsrewrite(IcStrategyCouple support, 
        Key<?>... keys) {
      for (Key<?> key : keys) {
        if (controlledKeysToStrategy.containsKey(key)) {
          throw new IllegalArgumentException();
        }
        controlledKeysToStrategy.put(key, support);
      }
      return this;
    }

    public Module buildClientModule() {
      return new ControllableInjectionClientModule(controlledKeysToStrategy);
    }

    public Module buildServerModule() {
      return new ControllableInjectionServerModule(controlledKeysToStrategy);
    }
  }
  
  public interface IcServer<T> {
    // TODO Key
    T getOverride();
  }
  
  public interface IcClient<T> {
    // TODO Key
    void setOverride(T override);
    //TODO void resetOverride(Class<?> clazz);
  }

  /**
   * On such class per injection controlling "strategy"
   * 
   * @author Luiz-Otavio Zorzella
   */
  public static class IcStrategyCouple {
    
    public interface IcServerStrategy {
      //TODO: kill <T>?
      <T> T getOverride(ControllableId pair);
    }
    
    public interface IcClientStrategy {
      //TODO: kill <T>?
      <T> void setOverride(ControllableId pair, T override);
    }
    
    private final Class<? extends IcClientStrategy> clientControllerSupport;
    private final Class<? extends IcServerStrategy> serverControllerSupport;

    public IcStrategyCouple(
        Class<? extends IcClientStrategy> clientControllerSupport,
        Class<? extends IcServerStrategy> serverControllerSupport
        ) {
      this.clientControllerSupport = clientControllerSupport;
      this.serverControllerSupport = serverControllerSupport;
    }
    
    Class<? extends IcClientStrategy> clientControllerClass() {
      return clientControllerSupport;
    }
    Class<? extends IcServerStrategy> serverControllerClass() {
      return serverControllerSupport;
    }
  }
  
  // TODO Key
  public static class ControllableId {
    private final TestId testId;
    private final Key<?> key;

    public ControllableId(TestId test, Key<?> key) {
      this.testId = test;
      this.key = key;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
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
      ControllableId other = (ControllableId) obj;
      if (key == null) {
        if (other.key != null)
          return false;
      } else if (!key.equals(other.key))
        return false;
      if (testId == null) {
        if (other.testId != null)
          return false;
      } else if (!testId.equals(other.testId))
        return false;
      return true;
    }
  }
  
  public static final class LocalJvmIcStrategy {

    private static final Map<ControllableId,Object> map = Maps.newHashMap();

    public IcStrategyCouple getControllerSupport() {
      return new IcStrategyCouple(MyClientController.class, MyServerController.class);
    }
    
    private static final class MyClientController implements IcClientStrategy {
      public <T> void setOverride(ControllableId pair, T override) {
        map.put(pair, override);
      }
    }
    
    private static final class MyServerController implements IcServerStrategy {
      @SuppressWarnings("unchecked")
      public <T> T getOverride(ControllableId pair) {
        return (T) map.get(pair);
      }
    }
  }
}

