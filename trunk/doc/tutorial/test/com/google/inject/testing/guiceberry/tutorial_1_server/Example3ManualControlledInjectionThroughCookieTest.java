package com.google.inject.testing.guiceberry.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.inject.testing.guiceberry.tutorial_1_server.prod_2_manual_controllable_injection.MyPetStoreServer.PetStoreModule;

@GuiceBerryEnv(Tutorial1Envs.MANUAL_CONTROLLABLE_INJECTION_THROUGH_COOKIE_PET_STORE_AT_8080_ENV)
public class Example3ManualControlledInjectionThroughCookieTest extends GuiceBerryJunit3TestCase {

  @Inject
  WelcomeTestPage welcomeTestPage;
  
  @Inject
  private TestId testId;
  
  public void testDogAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.DOG;
    // Create a stub instance of the mapper that always returns "DOG" as the
    // country
    PetStoreModule.override.put(testId, expected);
    // register a tearDown, so that at the end of the test, the override is null again
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
    // Create a stub instance of the mapper that always returns "DOG" as the
    // country
    PetStoreModule.override.put(testId, expected);
    // register a tearDown, so that at the end of the test, the override is null again
    addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModule.override.remove(testId);
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }
}
