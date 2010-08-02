package junit3_tdtc.tutorial_1_server;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.controllable.InjectionController;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.inject.Inject;

import tutorial_1_server.prod.Pet;
import tutorial_1_server.prod.PetOfTheMonth;
import tutorial_1_server.testing.PetStoreEnv4CanonicalSameJvmControllablePotm;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example4CanonicalSameJvmControllableInjectionTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AutoTearDownGuiceBerry.setUp(this, PetStoreEnv4CanonicalSameJvmControllablePotm.class);
  }
  
  @Inject
  WelcomeTestPage welcomeTestPage;
  
  @Inject
  @PetOfTheMonth
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
