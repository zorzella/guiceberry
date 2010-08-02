package junit3_tdtc.tutorial_1_server;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.inject.Inject;

import tutorial_1_server.testing.PetStoreEnv0Simple;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example1PageObjectsTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AutoTearDownGuiceBerry.setUp(this, PetStoreEnv0Simple.class);
  }

  @Inject
  WelcomeTestPage welcomeTestPage;

  public void testPetStoreWelcomeMessage() {
    welcomeTestPage
        .goTo()
        .assertWelcomeMessageIs("Welcome!");
  }

  public void testPetStoreTitle() {
    welcomeTestPage
        .goTo()
        .assertTitleIs("Welcome to the pet store");
  }
}
