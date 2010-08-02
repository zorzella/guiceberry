package junit3.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;
import com.google.inject.Inject;

import junit.framework.TestCase;

import tutorial_1_server.prod.Pet;
import tutorial_1_server.testing.PetStoreEnv2GlobalStaticControllablePotm;
import tutorial_1_server.testing.PetStoreEnv2GlobalStaticControllablePotm.PetStoreModuleWithGlobalStaticOverride;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example2ManualControlledInjectionTest extends TestCase {

  private TearDown toTearDown;
  
  @Override
  protected void tearDown() throws Exception {
    toTearDown.tearDown();
    super.tearDown();
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    toTearDown = ManualTearDownGuiceBerry.setUp(this, PetStoreEnv2GlobalStaticControllablePotm.class);
  }
  
  @Inject
  TearDownAccepter tearDownAccepter;
  
  @Inject
  WelcomeTestPage welcomeTestPage;

  public void testDogAsPotm() {
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
    welcomeTestPage.assertPetOfTheMonth(expected);
  }

  public void testCatAsPotm() {
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
    welcomeTestPage.assertPetOfTheMonth(expected);
  }
}
