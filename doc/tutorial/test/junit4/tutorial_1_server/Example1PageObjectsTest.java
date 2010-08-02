package junit4.tutorial_1_server;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

import org.junit.Rule;
import org.junit.Test;

import tutorial_1_server.testing.PetStoreEnv2GlobalStaticControllablePotm;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example1PageObjectsTest {

  @Rule
  public GuiceBerryRule guiceBerry = 
    new GuiceBerryRule(PetStoreEnv2GlobalStaticControllablePotm.class);

  @Inject
  WelcomeTestPage welcomeTestPage;

  @Test
  public void testMyServletDiv() {
    welcomeTestPage.goTo();
    welcomeTestPage.assertWelcomeMessage();
  }

  @Test
  public void testMyServletTitle() {
    welcomeTestPage.goTo();
    welcomeTestPage.assertTitle();
  }
}
