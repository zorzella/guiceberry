package com.google.inject.testing.guiceberry.tutorial;

import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Env;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@GuiceBerryEnv("com.google.inject.testing.guiceberry.tutorial.Example6StartingAnHttpServerTest$ExampleGuiceBerryEnv")
public class Example6StartingAnHttpServerTest extends GuiceBerryJunit3TestCase {

  private static final int PORT_NUMBER = 8080;

  @Inject
  MyHttpServer server;
  
  public static void testMyServlet() {
    WebDriver driver = new HtmlUnitDriver();
    driver.get("http://localhost:" + PORT_NUMBER);
    WebElement element = driver.findElement(By.xpath("//div"));
    assertEquals("Foo", element.getText());
  }

  public static final class ExampleGuiceBerryEnv extends GuiceBerryJunit3Env {
    
    @Override
    protected void configure() {
      super.configure();
      bind(MyHttpServer.class).in(Scopes.SINGLETON);
    }
    
    @Override
    protected Class<? extends TestScopeListener> getTestScopeListener() {
      return NoOpTestScopeListener.class;
    }
  }

  public static class MyHttpServer {
    public MyHttpServer() {
      Server server = new Server(PORT_NUMBER);    
      Context root = new Context(server, "/", Context.SESSIONS);
      root.addServlet(new ServletHolder(new MyServlet()), "/*");
      try {
        server.start();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  private static final class MyServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {
      resp.setContentType("text/html");
      PrintWriter writer = resp.getWriter();
      writer.println(
          "<html>\n" +
          "<head>\n" +
          "<title>Bar</title>\n" +
          "</head>\n" +
          "<body>\n" +
          "<div>Foo</div>\n" +
          "</body>\n" +
          "</html>\n");
      writer.flush();
      writer.close();
      req.getInputStream().close();
    }
  }
}
