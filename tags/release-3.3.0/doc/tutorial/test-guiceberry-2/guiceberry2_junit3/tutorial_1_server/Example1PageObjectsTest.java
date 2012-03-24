package guiceberry2_junit3.tutorial_1_server;

import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;

@GuiceBerryEnv(Tutorial1Envs.PET_STORE_ENV_0_SIMPLE)
public class Example1PageObjectsTest extends GuiceBerryJunit3TestCase {

  @Inject
  WelcomeTestPage welcomeTestPage;
  
  public void testMyServletDiv() {
    welcomeTestPage.goTo();
    welcomeTestPage.assertWelcomeMessage();
  }

  public void testMyServletTitle() {
    welcomeTestPage.goTo();
    welcomeTestPage.assertTitle();
  }
}
