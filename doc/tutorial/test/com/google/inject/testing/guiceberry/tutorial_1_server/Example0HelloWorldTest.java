package com.google.inject.testing.guiceberry.tutorial_1_server;

import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.inject.testing.guiceberry.tutorial_1_server.RegularPetStoreAt8080Env.PortNumber;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@GuiceBerryEnv(Tutorial1Envs.REGULAR_PET_STORE_AT_8080_ENV)
public class Example0HelloWorldTest extends GuiceBerryJunit3TestCase {

  @Inject
  WebDriver driver;
  
  @Inject
  @PortNumber
  int portNumber;
  
  public void testMyServletDiv() {
    driver.get("http://localhost:" + portNumber);
    WebElement element = driver.findElement(By.xpath("//div[@id='welcome']"));
    assertEquals("Welcome!", element.getText());
  }

  public void testMyServletTitle() {
    driver.get("http://localhost:" + portNumber);
    assertEquals("Welcome to the pet store", driver.getTitle());
  }
}
