package junit4.tutorial_1_server;

import com.google.guiceberry.controllable.InjectionController;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

import org.junit.Rule;
import org.junit.Test;

import tutorial_1_server.prod.Pet;
import tutorial_1_server.prod.PetOfTheMonth;
import tutorial_1_server.testing.PetStoreEnv4CanonicalSameJvmControllablePotm;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example4CanonicalSameJvmControllableInjectionTest {

  @Rule
  public GuiceBerryRule guiceBerry = 
    new GuiceBerryRule(PetStoreEnv4CanonicalSameJvmControllablePotm.class);
  
  @Inject
  WelcomeTestPage welcomeTestPage;
  
  @Inject
  @PetOfTheMonth
  private InjectionController<Pet> petOfTheMonthIc;

  @Test
  public void testDogAsPotm() {
    Pet expected = Pet.DOG;
    petOfTheMonthIc.setOverride(expected);
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }

  @Test
  public void testCatAsPotm() {
    Pet expected = Pet.CAT;
    petOfTheMonthIc.setOverride(expected);
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }
}
