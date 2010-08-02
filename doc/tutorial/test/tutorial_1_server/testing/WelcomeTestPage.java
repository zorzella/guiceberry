package tutorial_1_server.testing;

import com.google.inject.Inject;

import junit.framework.TestCase;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import tutorial_1_server.prod.Pet;

public final class WelcomeTestPage {

  @Inject
  WebDriver driver;
  
  @Inject
  @PortNumber
  int portNumber;
  
  public WelcomeTestPage goTo() {
    driver.get("http://localhost:" + portNumber);
    return this;
  }
  
  public WelcomeTestPage assertWelcomeMessageIs(String message) {
    WebElement element = driver.findElement(By.xpath("//div[@id='welcome']"));
    TestCase.assertEquals(message, element.getText());
    return this;
  }
  
  public WelcomeTestPage assertTitleIs(String title) {
    TestCase.assertEquals(title, driver.getTitle());
    return this;
  }

  public WelcomeTestPage assertPetOfTheMonthIs(Pet petOfTheMonth) {
    WebElement element = driver.findElement(By.xpath("//div[@id='potm']"));
    TestCase.assertEquals(petOfTheMonth.toString(), element.getText());
    return this;
  }
}