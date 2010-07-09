package junit4.tutorial_1_server;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.controllable.IcClient;

import org.junit.Rule;
import org.junit.Test;

import junit4.tutorial_1_server.prod.PetOfTheMonth;

public class Example4CanonicalSameJvmControllableInjectionTest {

  @Rule
  public GuiceBerryRule guiceBerry = 
    new GuiceBerryRule(PetStoreEnv4CanonicalSameJvmControllablePotm.class);
  
  @Inject
  WelcomeTestPage welcomeTestPage;
  
  @Inject
  private IcClient<PetOfTheMonth> petOfTheMonthIc;

  @Test
  public void testDogAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.DOG;
    petOfTheMonthIc.setOverride(expected);
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }

  @Test
  public void testCatAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.CAT;
    petOfTheMonthIc.setOverride(expected);
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }
}
