package tutorial_1_server.prod;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

public class MyPetStoreServer {
  
  private final Server server;
  private final int portNumber;
  
  public MyPetStoreServer() {
    this.portNumber = findFreePort();
    server = new Server(this.portNumber);    
    Context root = new Context(server, "/", Context.SESSIONS);
    
    root.addFilter(GuiceFilter.class, "/*", 0);
    root.addServlet(DefaultServlet.class, "/");
  }
  
  private static final int findFreePort() {
    for(int i = 8000; i < 8100; i++) {
      ServerSocket socket;
      try {
        socket = new ServerSocket(i);
        socket.close();
        return i;
      } catch (IOException portInUse) {}
    }
    throw new RuntimeException("Can't find a free port");
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
  
  public int getPortNumber() {
    return portNumber;
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
    protected PetOfTheMonth getPetOfTheMonth() {
      return somePetOfTheMonth();
    }
    
    private final Random rand = new Random();

    /** Simulates a call to a non-deterministic service -- maybe an external
     * server, maybe a DB call to a volatile entry, etc.
     */
    protected PetOfTheMonth somePetOfTheMonth() {
      PetOfTheMonth[] allPetsOfTheMonth = PetOfTheMonth.values();
      return allPetsOfTheMonth[(rand.nextInt(allPetsOfTheMonth.length))];
    }

    @Override
    protected void configure() {
      install(new MyServletModule());
    }
  }

  public static void main(String[] args) throws Exception {
    MyPetStoreServer petStoreServer = new MyPetStoreServer();
    petStoreServer.start();
    Thread.sleep(20000);
    petStoreServer.server.stop();
  }
}