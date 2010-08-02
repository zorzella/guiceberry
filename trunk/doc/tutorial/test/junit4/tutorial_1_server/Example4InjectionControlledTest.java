package junit4.tutorial_1_server;

import com.google.guiceberry.controllable.InjectionController;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

import org.junit.Rule;
import org.junit.Test;

import tutorial_1_server.prod.Pet;
import tutorial_1_server.prod.Featured;
import tutorial_1_server.testing.PetStoreEnv4InjectionControlled;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example4InjectionControlledTest {

  @Rule
  public GuiceBerryRule guiceBerry = 
    new GuiceBerryRule(PetStoreEnv4InjectionControlled.class);
  
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
