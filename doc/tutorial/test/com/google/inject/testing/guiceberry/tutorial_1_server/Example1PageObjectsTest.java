package com.google.inject.testing.guiceberry.tutorial_1_server;

import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.inject.testing.guiceberry.tutorial_1_server.RegularPetStoreAt8080Env.PortNumber;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@GuiceBerryEnv(Tutorial1Envs.REGULAR_PET_STORE_AT_8080_ENV)
public class Example1PageObjectsTest extends GuiceBerryJunit3TestCase {

  @Inject
  WelcomeTestPage welcomeTestPage;
  
  public void testMyServlet() {
    welcomeTestPage.assertWelcomeMessage();
  }
  
  public static final class WelcomeTestPage {
    @Inject
    WebDriver driver;
    
    @Inject
    @PortNumber
    int portNumber;
    
    public void goTo() {
      driver.get("http://localhost:" + portNumber);
    }
    
    public void assertWelcomeMessage() {
      WebElement element = driver.findElement(By.xpath("//div[@id='welcome']"));
      assertEquals("Welcome!", element.getText());
    }
  }
}
