package junit4.tutorial_1_server;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

import org.junit.Rule;
import org.junit.Test;

import tutorial_1_server.testing.PetStoreEnv0Simple;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example1PageObjectsTest {

  @Rule
  public GuiceBerryRule guiceBerry = new GuiceBerryRule(PetStoreEnv0Simple.class);

  @Inject
  WelcomeTestPage welcomeTestPage;

  @Test
  public void testPetStoreWelcomeMessage() {
    welcomeTestPage
      .goTo()
      .assertWelcomeMessageIs("Welcome!");
  }

  @Test
  public void testPetStoreTitle() {
    welcomeTestPage
      .goTo()
      .assertTitleIs("Welcome to the pet store");
  }
}
