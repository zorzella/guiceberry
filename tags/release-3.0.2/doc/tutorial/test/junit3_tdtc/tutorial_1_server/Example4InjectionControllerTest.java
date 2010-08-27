package junit3_tdtc.tutorial_1_server;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.controllable.InjectionController;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.inject.Inject;

import tutorial_1_server.prod.Pet;
import tutorial_1_server.prod.Featured;
import tutorial_1_server.testing.PetStoreEnv4InjectionController;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example4InjectionControllerTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AutoTearDownGuiceBerry.setUp(this, PetStoreEnv4InjectionController.class);
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
