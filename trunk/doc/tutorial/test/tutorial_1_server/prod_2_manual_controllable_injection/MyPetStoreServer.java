package tutorial_1_server.prod_2_manual_controllable_injection;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import tutorial_1_server.PetOfTheMonth;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyPetStoreServer {
  
  private final Server server;
  private final int portNumber;
  
  public MyPetStoreServer(int portNumber) {
    this.portNumber = portNumber;
    server = new Server(portNumber);    
    Context root = new Context(server, "/", Context.SESSIONS);
    
    MyServlet myServlet = new MyServlet();
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
    return Guice.createInjector(module, new ServletModule());
  }

  private static final class MyServlet extends HttpServlet {

    @Inject
    private Provider<PetOfTheMonth> petOfTheMonth;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
    throws IOException {
      resp.setContentType("text/html");
      PrintWriter writer = resp.getWriter();
      writer.println(
          "<html>\n" +
          "<head>\n" +
          "<title>Welcome to the pet store</title>\n" +
          "</head>\n" +
          "<body>\n" +
          "<div id='welcome'>Welcome!</div>\n" +
          "Your pet of the month is a: <div id='potm'>" + petOfTheMonth.get() + "</div>\n" +
          "</body>\n" +
          "</html>\n");
      writer.flush();
      writer.close();
      req.getInputStream().close();
    }
  }

  public static final class PetStoreModule extends AbstractModule {

    // !!!HERE!!!!
    public static PetOfTheMonth override;
    
    @Provides
    PetOfTheMonth getPetOfTheMonth() {
      // !!!HERE!!!!
      if (override != null) {
        return override;
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
    protected void configure() {}
  }

  public static void main(String[] args) throws Exception {
    MyPetStoreServer petStoreServer = new MyPetStoreServer(8080);
    petStoreServer.start();
    Thread.sleep(20000);
    petStoreServer.server.stop();
  }
}