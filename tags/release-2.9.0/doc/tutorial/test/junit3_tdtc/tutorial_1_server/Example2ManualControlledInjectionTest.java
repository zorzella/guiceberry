package junit3_tdtc.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.inject.Inject;

import tutorial_1_server.prod.PetOfTheMonth;

import junit3_tdtc.tutorial_1_server.PetStoreEnv2GlobalStaticControllablePotm.PetStoreModuleWithGlobalStaticOverride;

public class Example2ManualControlledInjectionTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AutoTearDownGuiceBerry.setUp(this, PetStoreEnv2GlobalStaticControllablePotm.class);
  }

  @Inject
  TearDownAccepter tearDownAccepter;
  
  @Inject
  WelcomeTestPage welcomeTestPage;

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
