package tutorial_1_server.prod_3_manual_controllable_injection_through_cookies;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.controllable.TestIdServerModule;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import tutorial_1_server.WelcomePageServlet;
import tutorial_1_server.PetOfTheMonth;

import java.util.Map;
import java.util.Random;

public class MyPetStoreServer {
  
  private final Server server;
  private final int portNumber;
  
  public MyPetStoreServer(int portNumber) {
    this.portNumber = portNumber;
    server = new Server(portNumber);    
    Context root = new Context(server, "/", Context.SESSIONS);
    
    WelcomePageServlet myServlet = new WelcomePageServlet();
    root.addServlet(new ServletHolder(myServlet), "/*");
    root.addFilter(GuiceFilter.class, "/", 0);

    Injector injector = getInjector();
    injector.injectMembers(myServlet);
  }
  
  public void start() {
    try {
      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public int getPortNumber() {
    return portNumber;
  }

  private Injector getInjector() {
    Module module = new PetStoreModule();
    // !!!HERE!!!!
    return Guice.createInjector(module, new TestIdServerModule());
  }

  public static final class PetStoreModule extends AbstractModule {

    public static final Map<TestId, PetOfTheMonth> override = Maps.newHashMap();

    @Provides
    // !!!HERE!!!!
    PetOfTheMonth getPetOfTheMonth(TestId testId) {
      PetOfTheMonth petOfTheMonth = override.get(testId);
      if (petOfTheMonth != null) {
        return petOfTheMonth;
      }
      return somePetOfTheMonth();
    }

    private final Random rand = new Random();

    /** Simulates a call to a non-deterministic service -- maybe an external
     * server, maybe a DB call to a volatile entry, etc.
     */
    private PetOfTheMonth somePetOfTheMonth() {
      PetOfTheMonth[] allPetsOfTheMonth = PetOfTheMonth.values();
      return allPetsOfTheMonth[(rand.nextInt(allPetsOfTheMonth.length))];
    }

    @Override
    protected void configure() {
      install(new ServletModule());
    }
  }

  public static void main(String[] args) throws Exception {
    MyPetStoreServer petStoreServer = new MyPetStoreServer(8080);
    petStoreServer.start();
    Thread.sleep(20000);
    petStoreServer.server.stop();
  }
}