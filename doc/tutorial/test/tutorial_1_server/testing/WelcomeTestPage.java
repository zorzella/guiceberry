package tutorial_1_server.testing;

import static org.junit.Assert.assertEquals;

import com.google.inject.Inject;

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
    assertEquals(message, element.getText());
    return this;
  }
  
  public WelcomeTestPage assertTitleIs(String title) {
    assertEquals(title, driver.getTitle());
    return this;
  }

  public WelcomeTestPage assertFeaturedPetIs(Pet pet) {
    WebElement element = driver.findElement(By.xpath("//div[@id='featured-pet']"));
    assertEquals(pet.toString(), element.getText());
    return this;
  }
}