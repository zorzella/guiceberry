package junit3.tutorial_1_server.prod;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public final class WelcomePageServlet extends HttpServlet {

  private Provider<PetOfTheMonth> petOfTheMonth;

  @Inject
  public WelcomePageServlet(Provider<PetOfTheMonth> petOfTheMonth) {
    this.petOfTheMonth = petOfTheMonth;
  }

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