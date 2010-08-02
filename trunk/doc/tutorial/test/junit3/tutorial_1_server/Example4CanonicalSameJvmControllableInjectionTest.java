package junit3.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.guiceberry.controllable.InjectionController;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;
import com.google.inject.Inject;

import junit.framework.TestCase;

import tutorial_1_server.prod.Pet;
import tutorial_1_server.testing.PetStoreEnv4CanonicalSameJvmControllablePotm;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example4CanonicalSameJvmControllableInjectionTest extends TestCase {

  private TearDown toTearDown;
  
  @Override
  protected void tearDown() throws Exception {
    toTearDown.tearDown();
    super.tearDown();
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    toTearDown = ManualTearDownGuiceBerry.setUp(this, PetStoreEnv4CanonicalSameJvmControllablePotm.class);
  }
  
  @Inject
  WelcomeTestPage welcomeTestPage;
  
  @Inject
  private InjectionController<Pet> petOfTheMonthIc;

  public void testDogAsPotm() {
    Pet expected = Pet.DOG;
    petOfTheMonthIc.setOverride(expected);
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }

  public void testCatAsPotm() {
    Pet expected = Pet.CAT;
    petOfTheMonthIc.setOverride(expected);
    welcomeTestPage.goTo();
    welcomeTestPage.assertPetOfTheMonth(expected);
  }
}
