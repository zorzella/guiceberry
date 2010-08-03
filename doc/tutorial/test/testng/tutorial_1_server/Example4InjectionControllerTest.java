package testng.tutorial_1_server;

import java.lang.reflect.Method;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.testing.TearDown;
import com.google.guiceberry.controllable.InjectionController;
import com.google.guiceberry.testng.TestNgGuiceBerry;
import com.google.inject.Inject;

import tutorial_1_server.prod.Pet;
import tutorial_1_server.prod.Featured;
import tutorial_1_server.testing.PetStoreEnv4InjectionController;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example4InjectionControllerTest {

  private TearDown toTearDown;
  
  @AfterMethod
  protected void tearDown() throws Exception {
    toTearDown.tearDown();
  }
  
  @BeforeMethod
  protected void setUp(Method m) throws Exception {
    toTearDown = TestNgGuiceBerry.setUp(this, m, PetStoreEnv4InjectionController.class);
  }
  
  @Inject
  WelcomeTestPage welcomeTestPage;
  
  @Inject
  @Featured
  private InjectionController<Pet> featuredPetInjectionController;

  @Test
  public void testWhenDogIsFeatured() {
    Pet expected = Pet.DOG;
    featuredPetInjectionController.setOverride(expected);
    welcomeTestPage
        .goTo()
        .assertFeaturedPetIs(expected);
  }

  @Test
  public void testWhenCatIsFeatured() {
    Pet expected = Pet.CAT;
    featuredPetInjectionController.setOverride(expected);
    welcomeTestPage
        .goTo()
        .assertFeaturedPetIs(expected);
  }
}
