package tutorial_1_server;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.internal.Maps;
import com.google.inject.internal.Objects;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.controllable.IcClient;
import com.google.inject.testing.guiceberry.controllable.IcServer;
import com.google.inject.testing.guiceberry.controllable.InterceptingBindingsBuilder;
import com.google.inject.testing.guiceberry.controllable.ProvisionInterceptor;
import com.google.inject.testing.guiceberry.controllable.TestIdServerModule;
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
import java.util.Collection;
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
  MyPetStoreServer startServer(final IcMaster icMaster) {
    MyPetStoreServer result = new MyPetStoreServer(8080) {
      @Override
      protected List<? extends Module> getModules() {
        List<Module> modules = new ArrayList<Module>();
        // !!! HERE !!!
        modules.add(new TestIdServerModule());
        // TODO: add rewritten modules instead
        modules.addAll(super.getModules());
        return ImmutableList.of(icMaster.buildServerModule(modules));
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
        bind(wrap(IcClient.class, e.getKey()))
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
          @SuppressWarnings("unchecked")
          public void setOverride(T override) {
            if (override == null) {
              throw new NullPointerException();
            }
            clientControllerSupportProvider.get().setOverride(
                new ControllableId(testIdProvider.get(), key), override);
          }

          @SuppressWarnings("unchecked")
          public void resetOverride() {
            clientControllerSupportProvider.get().setOverride(
                new ControllableId(testIdProvider.get(), key), null);
          }
        };
      }
    }
  }
  
  static Key<?> wrap(Type raw, Key<?> annotationHolder) {
    Type type = Types.newParameterizedType(raw, annotationHolder.getTypeLiteral().getType());
    if (annotationHolder.getAnnotation() != null) {
      return Key.get(type, annotationHolder.getAnnotation());
    } else if (annotationHolder.getAnnotationType() != null) {
      return Key.get(type, annotationHolder.getAnnotationType());
    } else {
      return Key.get(type);
    }
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
        bind(wrap(IcServer.class, e.getKey()))
             .toProvider(new MyServerProvider(e.getKey(), getProvider(TestId.class), 
               getProvider(e.getValue().serverControllerClass())));
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
          public T getOverride(Provider<? extends T> delegate) {
            return serControllerSupportProvider.get().getOverride(
                new ControllableId<T>(testIdProvider.get(), key), delegate);
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
    
    private static class ProvisionInterceptorModule extends AbstractModule {

      @Override
      protected void configure() {
        bind(ProvisionInterceptor.class).to(MyProvisionInterceptor.class);
      }
      
      private static class MyProvisionInterceptor implements ProvisionInterceptor {

        @Inject Injector injector;
        
        @SuppressWarnings("unchecked")
        public <T> T intercept(Key<T> key, Provider<? extends T> delegate) {
          IcServer<T> instance = (IcServer<T>) injector.getInstance(wrap(IcServer.class, key));
          return instance.getOverride(delegate);
        }
      }
    }

    public Module buildServerModule(final Collection<? extends Module> modules) {
      return new InterceptingBindingsBuilder()
        .install(modules)
        .install(new ProvisionInterceptorModule())
        .install(new ControllableInjectionServerModule(controlledKeysToStrategy))
        .intercept(controlledKeysToStrategy.keySet())
        .build();
    }
  }
  
  /**
   * On such class per injection controlling "strategy"
   * 
   * @author Luiz-Otavio Zorzella
   */
  public static class IcStrategyCouple {
    
    public interface IcServerStrategy {
      <T> T getOverride(ControllableId<T> pair, Provider<? extends T> delegate);
    }
    
    public interface IcClientStrategy {
      <T> void setOverride(ControllableId<T> pair, T override);
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
  
  public static class ControllableId<T> {
    private final TestId testId;
    private final Key<T> key;

    public ControllableId(TestId test, Key<T> key) {
      this.testId = test;
      this.key = key;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(testId, key);
    }

    @Override
    public boolean equals(Object other) {
      if (other instanceof ControllableId) {
        ControllableId<?> that = (ControllableId<?>) other;
        return Objects.equal(this.testId, that.testId)
          && Objects.equal(this.key, that.key);
      }
      return false;
    }
  }
  
  public static final class LocalJvmIcStrategy {

    private static final Map<ControllableId<?>,Object> map = Maps.newHashMap();

    public IcStrategyCouple getControllerSupport() {
      return new IcStrategyCouple(MyClientController.class, MyServerController.class);
    }
    
    private static final class MyClientController implements IcClientStrategy {
      public <T> void setOverride(ControllableId<T> pair, T override) {
        map.put(pair, override);
      }
    }
    
    private static final class MyServerController implements IcServerStrategy {
      @SuppressWarnings("unchecked")
      public <T> T getOverride(ControllableId<T> pair, Provider<? extends T> delegate) {
        if (!map.containsKey(pair)) {
          return delegate.get();
        }
        return (T) map.get(pair);
      }
    }
  }
}

