package com.google.inject.testing.guiceberry.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.inject.testing.guiceberry.tutorial_1_server.prod_1_manual_controllable_injection.MyPetStoreServer.PetStoreModule;

@GuiceBerryEnv(Tutorial1Envs.MANUAL_CONTROLLABLE_INJECTION_PET_STORE_AT_8080_ENV)
public class Example2ManualControlledInjectionTest extends GuiceBerryJunit3TestCase {

  @Inject
  WelcomeTestPage welcomeTestPage;
  
  public void testDogAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.DOG;
    // Create a stub instance of the mapper that always returns "DOG" as the
    // country
    PetStoreModule.override = expected;
    // register a tearDown, so that at the end of the test, the override is null again
    addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModule.override = null;
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }

  public void testCatAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.CAT;
    // Create a stub instance of the mapper that always returns "DOG" as the
    // country
    PetStoreModule.override = expected;
    // register a tearDown, so that at the end of the test, the override is null again
    addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModule.override = null;
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }
}
