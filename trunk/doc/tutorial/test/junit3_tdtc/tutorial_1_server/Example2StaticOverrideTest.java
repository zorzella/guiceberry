package junit3_tdtc.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.inject.Inject;

import tutorial_1_server.prod.Pet;
import tutorial_1_server.testing.PetStoreEnv2StaticOverride;
import tutorial_1_server.testing.PetStoreEnv2StaticOverride.PetStoreModuleWithGlobalStaticOverride;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example2StaticOverrideTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AutoTearDownGuiceBerry.setUp(this, PetStoreEnv2StaticOverride.class);
  }

  @Inject
  TearDownAccepter tearDownAccepter;
  
  @Inject
  WelcomeTestPage welcomeTestPage;

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
