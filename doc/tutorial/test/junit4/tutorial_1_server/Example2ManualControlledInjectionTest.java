package junit4.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

import org.junit.Rule;
import org.junit.Test;

import junit4.tutorial_1_server.PetStoreEnv2GlobalStaticControllablePotm.PetStoreModuleWithGlobalStaticOverride;
import junit4.tutorial_1_server.prod.PetOfTheMonth;

public class Example2ManualControlledInjectionTest {

  @Rule
  public GuiceBerryRule guiceBerry = 
    new GuiceBerryRule(PetStoreEnv2GlobalStaticControllablePotm.class);

  @Inject
  TearDownAccepter tearDownAccepter;
  
  @Inject
  WelcomeTestPage welcomeTestPage;

  @Test
  public void testDogAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.DOG;
    PetStoreModuleWithGlobalStaticOverride.override = expected;
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    tearDownAccepter.addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModuleWithGlobalStaticOverride.override = null;
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }

  @Test
  public void testCatAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.CAT;
    PetStoreModuleWithGlobalStaticOverride.override = expected;
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    tearDownAccepter.addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModuleWithGlobalStaticOverride.override = null;
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }
}
