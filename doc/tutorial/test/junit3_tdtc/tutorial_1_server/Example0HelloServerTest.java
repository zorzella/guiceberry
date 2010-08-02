package junit3_tdtc.tutorial_1_server;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.inject.Inject;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import tutorial_1_server.testing.PetStoreEnv0Simple;
import tutorial_1_server.testing.PortNumber;

public class Example0HelloServerTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AutoTearDownGuiceBerry.setUp(this, PetStoreEnv0Simple.class);
  }

  @Inject
  WebDriver driver;
  
  @Inject
  @PortNumber
  int portNumber;

  public void testPetStoreWelcomeMessage() {
    driver.get("http://localhost:" + portNumber);
    WebElement element = driver.findElement(By.xpath("//div[@id='welcome']"));
    assertEquals("Welcome!", element.getText());
  }

  public void testPetStoreTitle() {
    driver.get("http://localhost:" + portNumber);
    assertEquals("Welcome to the pet store", driver.getTitle());
  }
}
