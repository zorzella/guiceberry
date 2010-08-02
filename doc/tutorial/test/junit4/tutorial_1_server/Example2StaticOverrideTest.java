package junit4.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

import org.junit.Rule;
import org.junit.Test;

import tutorial_1_server.prod.Pet;
import tutorial_1_server.testing.PetStoreEnv2StaticOverride;
import tutorial_1_server.testing.PetStoreEnv2StaticOverride.PetStoreModuleWithGlobalStaticOverride;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example2StaticOverrideTest {

  @Rule
  public GuiceBerryRule guiceBerry = 
    new GuiceBerryRule(PetStoreEnv2StaticOverride.class);

  @Inject
  TearDownAccepter tearDownAccepter;
  
  @Inject
  WelcomeTestPage welcomeTestPage;

  @Test
  public void testWhenDogIsFeatured() {
    Pet expected = Pet.DOG;
    PetStoreModuleWithGlobalStaticOverride.override = expected;
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    tearDownAccepter.addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModuleWithGlobalStaticOverride.override = null;
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertFeaturedPetIs(expected);
  }

  @Test
  public void testWhenCatIsFeatured() {
    Pet expected = Pet.CAT;
    PetStoreModuleWithGlobalStaticOverride.override = expected;
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    tearDownAccepter.addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModuleWithGlobalStaticOverride.override = null;
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertFeaturedPetIs(expected);
  }
}
