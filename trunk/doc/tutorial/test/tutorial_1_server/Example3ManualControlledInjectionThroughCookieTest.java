package tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

import tutorial_1_server.prod_3_manual_controllable_injection_through_cookies.MyPetStoreServer.PetStoreModule;

@GuiceBerryEnv(Tutorial1Envs.MANUAL_CONTROLLABLE_INJECTION_THROUGH_COOKIE_PET_STORE_AT_8080_ENV)
public class Example3ManualControlledInjectionThroughCookieTest extends GuiceBerryJunit3TestCase {

  @Inject
  WelcomeTestPage welcomeTestPage;
  
  @Inject
  private TestId testId;
  
  public void testDogAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.DOG;
    PetStoreModule.override.put(testId, expected);
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModule.override.remove(testId);
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }

  public void testCatAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.CAT;
    PetStoreModule.override.put(testId, expected);
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModule.override.remove(testId);
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }
}
