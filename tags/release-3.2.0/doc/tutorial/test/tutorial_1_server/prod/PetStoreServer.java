package tutorial_1_server.prod;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;

import java.util.Random;

public class PetStoreServer {
  
  private final Server server;
  
  @Inject
  public PetStoreServer(int portNumber) {
    server = new Server(portNumber);
    Context root = new Context(server, "/", Context.SESSIONS);
    
    root.addFilter(GuiceFilter.class, "/*", 0);
    root.addServlet(DefaultServlet.class, "/");
  }
  
  public Injector start() {
    try {
      Injector result = Guice.createInjector(getPetStoreModule());
      server.start();
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  protected Module getPetStoreModule() {
    return new PetStoreModule();
  }
  
  private static final class MyServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
      serve("/*").with(WelcomePageServlet.class);
    }
  }

  public static class PetStoreModule extends AbstractModule {

    @Provides
    @Featured
    protected Pet getFeaturedPet() {
      return calculateFeaturedPet();
    }
    
    private final Random random = new Random();

    /** Let's simulate a call to a non-deterministic service -- e.g. an external
     * server, or a DB call to a volatile entry, etc.
     */
    protected Pet calculateFeaturedPet() {
      Pet[] allPets = Pet.values();
      return allPets[random.nextInt(allPets.length)];
    }

    @Override
    protected void configure() {
      install(new MyServletModule());
    }
  }

  public static void main(String[] args) throws Exception {
    PetStoreServer petStoreServer = new PetStoreServer(8888);
    petStoreServer.start();
    Thread.sleep(20000);
    petStoreServer.server.stop();
  }
}