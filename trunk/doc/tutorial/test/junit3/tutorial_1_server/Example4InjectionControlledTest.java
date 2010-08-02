package junit3.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.guiceberry.controllable.InjectionController;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;
import com.google.inject.Inject;

import junit.framework.TestCase;

import tutorial_1_server.prod.Pet;
import tutorial_1_server.prod.Featured;
import tutorial_1_server.testing.PetStoreEnv4InjectionControlled;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example4InjectionControlledTest extends TestCase {

  private TearDown toTearDown;
  
  @Override
  protected void tearDown() throws Exception {
    toTearDown.tearDown();
    super.tearDown();
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    toTearDown = ManualTearDownGuiceBerry.setUp(this, PetStoreEnv4InjectionControlled.class);
  }
  
  @Inject
  WelcomeTestPage welcomeTestPage;
  
  @Inject
  @Featured
  private InjectionController<Pet> featuredPetInjectionController;

  public void testWhenDogIsFeatured() {
    Pet expected = Pet.DOG;
    featuredPetInjectionController.setOverride(expected);
    welcomeTestPage
        .goTo()
        .assertFeaturedPetIs(expected);
  }

  public void testWhenCatIsFeatured() {
    Pet expected = Pet.CAT;
    featuredPetInjectionController.setOverride(expected);
    welcomeTestPage
        .goTo()
        .assertFeaturedPetIs(expected);
  }
}
