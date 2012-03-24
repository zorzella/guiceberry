package guiceberry2_junit3.tutorial_1_server;

import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.controllable.IcClient;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

import guiceberry2_junit3.tutorial_1_server.prod.PetOfTheMonth;


@GuiceBerryEnv(Tutorial1Envs.PET_STORE_ENV_4_CANONICAL_SAME_JVM_CONTROLLABLE_POTM)
public class Example4CanonicalSameJvmControllableInjectionTest extends GuiceBerryJunit3TestCase {

  @Inject
  WelcomeTestPage welcomeTestPage;
  
  @Inject
  private IcClient<PetOfTheMonth> petOfTheMonthIc;

  public void testDogAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.DOG;
    petOfTheMonthIc.setOverride(expected);
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }

  public void testCatAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.CAT;
    petOfTheMonthIc.setOverride(expected);
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }
}
