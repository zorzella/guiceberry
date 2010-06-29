package tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

import tutorial_1_server.PetStoreEnv2GlobalStaticControllablePotm.PetStoreModuleWithGlobalStaticOverride;
import tutorial_1_server.prod.PetOfTheMonth;

@GuiceBerryEnv(Tutorial1Envs.PET_STORE_ENV_2_GLOBAL_STATIC_POTM)
public class Example2ManualControlledInjectionTest extends GuiceBerryJunit3TestCase {

  @Inject
  WelcomeTestPage welcomeTestPage;
  
  public void testDogAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.DOG;
    PetStoreModuleWithGlobalStaticOverride.override = expected;
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModuleWithGlobalStaticOverride.override = null;
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }

  public void testCatAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.CAT;
    PetStoreModuleWithGlobalStaticOverride.override = expected;
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModuleWithGlobalStaticOverride.override = null;
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }
}
