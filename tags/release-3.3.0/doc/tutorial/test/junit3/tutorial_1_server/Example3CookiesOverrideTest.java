package junit3.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.guiceberry.TestId;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;
import com.google.inject.Inject;

import junit.framework.TestCase;

import tutorial_1_server.prod.Pet;
import tutorial_1_server.testing.PetStoreEnv3CookiesOverride;
import tutorial_1_server.testing.PetStoreEnv3CookiesOverride.PetStoreModuleWithTestIdBasedOverride;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example3CookiesOverrideTest extends TestCase {

  private TearDown toTearDown;
  
  @Override
  protected void tearDown() throws Exception {
    toTearDown.tearDown();
    super.tearDown();
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    toTearDown = ManualTearDownGuiceBerry.setUp(this, PetStoreEnv3CookiesOverride.class);
  }
  
  @Inject
  WelcomeTestPage welcomeTestPage;
  
  @Inject
  TearDownAccepter tearDownAccepter;
  
  @Inject
  private TestId testId;
  
  public void testWhenDogIsFeatured() {
    Pet expected = Pet.DOG;
    PetStoreModuleWithTestIdBasedOverride.override.put(testId, expected);
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    tearDownAccepter.addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModuleWithTestIdBasedOverride.override.remove(testId);
      }
    });
    welcomeTestPage
        .goTo()
        .assertFeaturedPetIs(expected);
  }

  public void testWhenCatIsFeatured() {
    Pet expected = Pet.CAT;
    PetStoreModuleWithTestIdBasedOverride.override.put(testId, expected);
    // register a tearDown, so that at the end of the test, 
    // the override is set to null again
    tearDownAccepter.addTearDown(new TearDown() {
      public void tearDown() {
        PetStoreModuleWithTestIdBasedOverride.override.remove(testId);
      }
    });
    welcomeTestPage
        .goTo()
        .assertFeaturedPetIs(expected);
  }
}
