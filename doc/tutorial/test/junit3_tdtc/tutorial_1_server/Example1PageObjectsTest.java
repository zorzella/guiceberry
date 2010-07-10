package junit3_tdtc.tutorial_1_server;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.junit3.AutoTearDownGuiceBerry;
import com.google.inject.Inject;

public class Example1PageObjectsTest extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AutoTearDownGuiceBerry.setup(this, PetStoreEnv2GlobalStaticControllablePotm.class);
  }

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
