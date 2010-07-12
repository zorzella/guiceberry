package junit3.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.guiceberry.TestId;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;
import com.google.inject.Inject;

import junit.framework.TestCase;
import junit3.tutorial_1_server.PetStoreEnv3CookiesControlledPotm.PetStoreModuleWithTestIdBasedOverride;
import junit3.tutorial_1_server.prod.PetOfTheMonth;

public class Example3ManualControlledInjectionThroughCookieTest extends TestCase {

  private TearDown toTearDown;
  
  @Override
  protected void tearDown() throws Exception {
    toTearDown.tearDown();
    super.tearDown();
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    toTearDown = ManualTearDownGuiceBerry.setUp(this, PetStoreEnv3CookiesControlledPotm.class);
  }
  
  @Inject
  WelcomeTestPage welcomeTestPage;
  
  @Inject
  TearDownAccepter tearDownAccepter;
  
  @Inject
  private TestId testId;
  
  public void testDogAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.DOG;
    PetStoreModuleWithTestIdBasedOverride.override.put(testId, expected);
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    tearDownAccepter.addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModuleWithTestIdBasedOverride.override.remove(testId);
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }

  public void testCatAsPotm() {
    PetOfTheMonth expected = PetOfTheMonth.CAT;
    PetStoreModuleWithTestIdBasedOverride.override.put(testId, expected);
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    tearDownAccepter.addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModuleWithTestIdBasedOverride.override.remove(testId);
      }
    });
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }
}
