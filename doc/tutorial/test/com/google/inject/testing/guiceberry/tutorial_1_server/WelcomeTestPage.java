package com.google.inject.testing.guiceberry.tutorial_1_server;

import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.tutorial_1_server.RegularPetStoreAt8080Env.PortNumber;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class WelcomeTestPage {
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
    Example1PageObjectsTest.assertEquals("Welcome!", element.getText());
  }
  
  public void assertTitle() {
    Example1PageObjectsTest.assertEquals("Welcome to the pet store", driver.getTitle());
  }
}