package junit3.tutorial_1_server;

import com.google.common.testing.TearDown;
import com.google.guiceberry.junit3.ManualTearDownGuiceBerry;
import com.google.inject.Inject;

import junit.framework.TestCase;

import tutorial_1_server.testing.PetStoreEnv0Simple;
import tutorial_1_server.testing.WelcomeTestPage;

public class Example1PageObjectsTest extends TestCase {

  private TearDown toTearDown;
  
  @Override
  protected void tearDown() throws Exception {
    toTearDown.tearDown();
    super.tearDown();
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    toTearDown = ManualTearDownGuiceBerry.setUp(this, PetStoreEnv0Simple.class);
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
