/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.testing.guiceberry.junit3;

import com.google.common.testing.TearDown;
import com.google.common.testing.junit3.JUnitAsserts;
import com.google.common.testing.junit3.TearDownTestCase;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.GuiceBerryEnvMain;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.TestScopeListener;

import junit.framework.TestCase;

/**
 * Tests the {@link GuiceBerryJunit3} class.
 * 
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
 */
public class GuiceBerryJunit3Test extends TearDownTestCase {
  
  /*
   * Java, unfortunately, provides no way to statically get a class' canonical name
   * through reflection.
   */
  private static final String SELF_CANONICAL_NAME = 
    "com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test";
  
  private static final String GUICE_BERRY_ENV_THAT_DOES_NOT_EXIST = 
    "com.this.guice.berry.env.does.NotExist";
  
  private static final String NOT_A_GUICE_BERRY_ENV_BECAUSE_IT_IS_ABSTRACT = 
    "com.google.inject.AbstractModule";
    
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    TearDown tearDown = new TearDown() {
      public void tearDown() throws Exception {    
        GuiceBerryJunit3.clear();
      }
    };
    addTearDown(tearDown);
  }

  public void testSelfCanonicalNameConstantIsCorrect() throws Exception {
    
    String message = 
      "The constant SELF_CANONICAL_NAME does not match this class's \n" +
      "canonical name (e.g. this class has just been moved or renamed).\n" +
      "\n" +
      "There's unfortunatelly no way to statically get a class' canonical \n" +
      "name through reflection, and, thus, this constant has to be manually\n" +
      "updated. \n" +
      "\n" +
      "Several tests will fail after this, until this is fixed.";
    assertEquals(message, 
        this.getClass().getCanonicalName(), SELF_CANONICAL_NAME);
    
  }
  
  public void testWithNoAnnotationThrowsException() {
    try {
      GuiceBerryJunit3.setUp(this);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals(
              "Test class " +
              "'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test' " +
              "must have an @GuiceBerryEnv annotation.", 
              expected.getMessage());
    }
  }
  
  public void testAnnotationToNonExistingGbeThrowsException() {
   try {
      TestWithNonExistingGbe test = TestWithNonExistingGbe.createInstance();
      GuiceBerryJunit3.setUp(test);
      fail();
   } catch (IllegalArgumentException expected) { 
       assertEquals(
               "@GuiceBerryEnv class " +
               "'com.this.guice.berry.env.does.NotExist' " +
               "was not found.", 
               expected.getMessage());
   }
  }
 
  public void testGbeThatHasMissingBindingsThrowsException() {   
    try {
      TestWithGbeThatHasMissingBindings test = 
        TestWithGbeThatHasMissingBindings.createInstance();
      GuiceBerryJunit3.setUp(test);
      fail();
    } catch (RuntimeException expected) {
      //TODO: we should assert expected's cause is ConfigurationException, but 
      //that exception is private
      assertEquals(ConfigurationException.class, expected.getCause().getClass());
      assertTrue(expected.getMessage().startsWith("Binding error in the module"));
      String configurationExceptionMeat = 
        "No implementation for " +
      	BarService.class.getName() +
      	" was bound.";
      assertTrue(expected.getCause().getMessage().contains(configurationExceptionMeat));
    }
  }
 
  public void testGbeThatNotImplementsModuleThrowsException() {
    try {
      TestWithGbeThatNotImplementsModule test = 
        TestWithGbeThatNotImplementsModule.createInstance();
      GuiceBerryJunit3.setUp(test);
      fail();
    } catch (IllegalArgumentException expected) {
        assertEquals("@GuiceBerryEnv class " +
                "'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$NotAGuiceBerryEnvOne' " +
                "must be a Guice Module (i.e. implement com.google.inject.Module).", 
                expected.getMessage());
    }
  }
     
  public void testGbeThatHasWrongConstructorThrowsException() {
    try {
      TestWithGbeThatHasAnIllegalConstructor test = 
        TestWithGbeThatHasAnIllegalConstructor.createInstance();
      GuiceBerryJunit3.setUp(test);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("@GuiceBerryEnv class " +
              "'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$GuiceBerryEnvWithIllegalConstructor' " +
              "must have a public zero-arguments constructor", 
              expected.getMessage());
    }
  }
 
  public void testGbeIsAbstractClassModuleThrowsException() {
    TestWithGbeThatIsAnAbstractClass test = 
      TestWithGbeThatIsAnAbstractClass.createInstance();
    try {
      GuiceBerryJunit3.setUp(test);
      fail();
    } catch (RuntimeException expected) {
      assertTrue(expected.getCause() instanceof InstantiationException);
    }
  }
  
  public void testSimpleValidGbe() {
    TestWithGbeOne test = TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(test);
    assertNotNull(test.barService);
    assertNotNull(test.fooService);
  }

  private static class MyThread extends Thread {
    private TestCase theTestCase;

    @Override
    public void run() {
      theTestCase = GuiceBerryJunit3.getActualTestCase();
    }
  }
  
  public void testThatTestScopeThreadLocalInherits() throws InterruptedException {
    TestWithGbeOne test = TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(test);

    // Finding the current test case from a secondary thread.
    MyThread myThread = new MyThread();
    myThread.start();   
    myThread.join();
    
    assertNotNull(myThread.theTestCase);
    assertEquals(TestWithGbeOne.class.getName(), 
        myThread.theTestCase.getClass().getName());
  }

  public void testThatTestCaseGetsInjectedWithWhatsConfiguredInTheGbe() {
    TestWithGbeOne test = TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(test);
    assertTrue(test.barService instanceof BarServiceOne);
    assertTrue(test.fooService instanceof FooServiceOne);
  }
  
  public void testInjectorMapIsSetAfterATest() throws ClassNotFoundException {
    TestWithGbeOne test = TestWithGbeOne.createInstance();
    Injector injector = GuiceBerryJunit3.moduleClassToInjectorMap.get(Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNull(injector);
    
    GuiceBerryJunit3.setUp(test);
    injector = GuiceBerryJunit3.moduleClassToInjectorMap.get(Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    
    assertNotNull(injector);
  }
  
  public void testThatTwoTestsWithSameGbeUseTheSameInjector() 
      throws ClassNotFoundException {
    NonTdtcForGbeOne testOne = NonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testOne);
    
    Injector injectorOne = GuiceBerryJunit3.moduleClassToInjectorMap.get(Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    GuiceBerryJunit3.tearDown(testOne);

    AnotherNonTdtcForGbeOne testTwo = AnotherNonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testTwo);
    
    Injector injectorTwo = 
      GuiceBerryJunit3.moduleClassToInjectorMap.get(Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));

    // "number" is bound to a random, so this will only pass if the injector
    // used for both tests was the same
    assertEquals(testOne.number, testTwo.number);
    // This would fail if the first injector was, say, thrown away and replaced
    // when running the second test
    assertSame(injectorOne, injectorTwo);    
    // This would fail if GuiceBerryJunit3 creates another injector for the
    // benefit of the second test
    assertEquals(1, GuiceBerryJunit3.numberOfInjectorsInUse());    
  }
  
  public void testNotReUsingInjectorForTestsThatDeclaresADifferentGbe() {
    NonTdtcForGbeOne testOne = NonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testOne);    
    GuiceBerryJunit3.tearDown(testOne);
    
    TestWithGbeTwo testTwo = TestWithGbeTwo.createInstance();
    GuiceBerryJunit3.setUp(testTwo);
    JUnitAsserts.assertNotEqual(testOne.number, testTwo.number);
  }

  public void testPutTwoInjectorsInMapForTestsThatDeclareDifferentGbes() {
    NonTdtcForGbeOne testOne = NonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testOne);
    GuiceBerryJunit3.tearDown(testOne);
    
    TestWithGbeTwo testTwo = TestWithGbeTwo.createInstance();
    GuiceBerryJunit3.setUp(testTwo);
   
    assertEquals(2, GuiceBerryJunit3.numberOfInjectorsInUse());    
  }
  
  public void testRemapper() {
    TestWithGbeOne test = TestWithGbeOne.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
      MyGuiceBerryEnvRemapper.class.getName());

    GuiceBerryJunit3.setUp(test);
    assertEquals(BarServiceTwo.class, test.barService.getClass());
    assertEquals(FooServiceTwo.class, test.fooService.getClass());
  }
  
  public void testRemapperThatReturnsNullGivesGoodErrorMessage() {
    TestWithGbeOne test = TestWithGbeOne.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
      MyGuiceBerryEnvRemapperThatReturnsNull.class.getName());

    try {
      GuiceBerryJunit3.setUp(test);
      fail("An exception should have been thrown.");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The installed GuiceBerryEnvRemapper " +
          "'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$MyGuiceBerryEnvRemapperThatReturnsNull' " +
          "returned 'null' for the 'fooTest' test, " +
          "which declares " +
          "'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$GuiceBerryEnvOne' " +
          "as its GuiceBerryEnv", 
          e.getMessage());
    }
  }
  
  public void testRemapperSystemPropertyNeedsClassThatExists() {
    TestWithGbeOne test = TestWithGbeOne.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
      "foo");

    try {
      GuiceBerryJunit3.setUp(test);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("Class 'foo', which is being declared as a GuiceBerryEnvRemapper, does not exist.", 
        expected.getMessage());
    }
  }
  
  public void testRemapperSystemPropertyNeedsClassThatImplementsCorrectInterface() {
    TestWithGbeOne test = TestWithGbeOne.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
          MyNonGuiceBerryEnvRemapper.class.getName());

    try {
      GuiceBerryJunit3.setUp(test);
    fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("Class 'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$MyNonGuiceBerryEnvRemapper' " +
        "is being declared as a GuiceBerryEnvRemapper, but does not implement that interface", 
        expected.getMessage());
    }
  }

  public void testRemapperSystemPropertyNeedsClassWithZeroArgConstructor() {
    TestWithGbeOne test = TestWithGbeOne.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
      MyGuiceBerryEnvRemapperWithInvalidConstructor.class.getName());

    try {
      GuiceBerryJunit3.setUp(test);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("GuiceBerryEnvRemapper 'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$MyGuiceBerryEnvRemapperWithInvalidConstructor' " +
        "must have public zero-arguments constructor", expected.getMessage());
    }
  }

  public void testInjectionOfTestId() {
    TestWithGbeOne test = TestWithGbeOne.createInstance();
    
    assertNull(test.testId);
    GuiceBerryJunit3.setUp(test);
    assertNotNull(test.testId);

    String expectedTestIdPrefix = 
      test.getName() + ":" + test.getClass().getName() + ":";

    // This will break if TestId.toString() changes format
    assertTrue(String.format(
        "'%s' should start with '%s'", test.testId.toString(), expectedTestIdPrefix),
        test.testId.toString().startsWith(expectedTestIdPrefix));
  } 
  
  public void testDifferentTestsGetInjectedWithDifferentTestIds() {
    NonTdtcForGbeOne testOne = NonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testOne);
    GuiceBerryJunit3.tearDown(testOne);
   
    AnotherTestWithGbeOne testTwo = AnotherTestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testTwo);
   
    JUnitAsserts.assertNotEqual(testOne.testId, testTwo.testId);
  }
  
  public void testInjectionOfTestCase() {  
    TestWithGbeOne test = TestWithGbeOne.createInstance();
    assertEquals(null, test.testCase);
    GuiceBerryJunit3.setUp(test);
    assertEquals(test.getName(), test.testCase.getName());
  } 
  
  
  public void testDifferentTestsGetsInjectedWithDifferentTestCases() {
    
    NonTdtcForGbeOne testOne = NonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testOne);    
    assertEquals(testOne.getName(), testOne.testCase.getName());
    GuiceBerryJunit3.tearDown(testOne);
   
    AnotherTestWithGbeOne testTwo = AnotherTestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testTwo);
    assertEquals(testTwo.getName(), testTwo.testCase.getName());
    
    JUnitAsserts.assertNotEqual(testOne.testCase, testTwo.testCase);
  }

  public void testMethodTearDownWorksProperly() {
    NonTdtcForGbeOne test = NonTdtcForGbeOne.createInstance();
    
    GuiceBerryJunit3.setUp(test);
    assertEquals(test,  GuiceBerryJunit3.getActualTestCase());
    GuiceBerryJunit3.tearDown(test); 
  //No concurrency problems as the actual TestCase is: ThreadLocal<TestCase>
    assertNull(GuiceBerryJunit3.getActualTestCase());
  }
  
  public void testMethodTearDownNoPreviousSetupOnClassWithNoAnnotation() {
    try {
      GuiceBerryJunit3.tearDown(UnAnnotatedNonTdtc.createInstance());
      fail();
    } catch (NullPointerException expected) {}
  }
  
  
  public void testMethodTearDownNoPreviousSetupOnClassWithAnnotation() {
    
    TestWithGbeOne test = TestWithGbeOne.createInstance();
    try {
      GuiceBerryJunit3.tearDown(test);
      fail();
    } catch (RuntimeException expected) {}  
  }
  
  
  public void testTearDownNoPreviousSetupOnClassWithAnnotationThatWasUsed() {

    TestWithGbeOne testOne = TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testOne);
    testOne.run();
    
    TestWithGbeOne testTwo = TestWithGbeOne.createInstance();
    try {
      GuiceBerryJunit3.tearDown(testTwo);
      fail();
    } catch (RuntimeException expected) {}
  }
  
  
  public void testTearDownOnDifferentClassThatSetupWasCalled() {
    TestWithGbeOne testOne = TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testOne);
    AnotherTestWithGbeOne testTwo = AnotherTestWithGbeOne.createInstance();
    try {
      GuiceBerryJunit3.tearDown(testTwo);
      fail();
    } catch (RuntimeException expected) {}
  }
  
 
  public void testCallingTwoSetupWithNoTearDownBetween() {
    TestWithGbeOne test = TestWithGbeOne.createInstance();
    try {
      GuiceBerryJunit3.setUp(test);
      GuiceBerryJunit3.setUp(test);
      fail();
    } catch (RuntimeException expected) {}   
  }
  
  public void testAddTearDownToTearDownTestCase() {
    TestWithGbeOne test = TestWithGbeOne.createInstance();
   
    GuiceBerryJunit3.setUp(test);
    assertEquals(test, test.testCase);
    test.run();
    //No concurrency problems as the actual TestCase is: ThreadLocal<TestCase>
    assertNull(GuiceBerryJunit3.getActualTestCase());
  }

  public void testTestCaseCanBeUsedInsteadOfTearDownTestCase() {
    NonTdtcForGbeOne test = NonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(test);
    test.run();
  }
  
  public void testMethodTearDownForTestCaseNotCalledAutomatically() {
    NonTdtcForGbeOne testOne = NonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testOne);
    testOne.run();

    TestWithGbeOne testTwo = TestWithGbeOne.createInstance();
    try {  
      GuiceBerryJunit3.setUp(testTwo);
      fail();
    } catch (RuntimeException expected) {}
  }
 
  public void testMethodTearDownForTestCaseCalledManually() {
    NonTdtcForGbeOne testOne = NonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testOne);   
    testOne.run();
 
    GuiceBerryJunit3.tearDown(testOne);
    TestWithGbeOne testTwo = TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testTwo);
    testTwo.run();
  }
 
  public void testGbeThaHasNoTestScopeListenerBinding() {
    TestWithGbeThatDoesNotBindATestScopeListener test = 
      TestWithGbeThatDoesNotBindATestScopeListener.createInstance();
    try {
      GuiceBerryJunit3.setUp(test);
      fail();
    } catch (RuntimeException expected) {
      assertEquals("TestScopeListener must be bound in your GuiceBerryEnv.", 
          expected.getMessage());
    }  
  }
  
  public void testGbeThatBindsTestScopeListenerToNoOpTestScopeListener() {
    TestWithGbeOne test = TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(test);
    
    assertTrue(test.testScopeListener instanceof NoOpTestScopeListener);    
  }
  
  public void testGbeWithCustomTestScopeListener() {
    TestWithGbeTwo test = TestWithGbeTwo.createInstance();
    GuiceBerryJunit3.setUp(test);

    assertTrue(test.testScopeListener instanceof BazService);    
  }

  public void testGbeWithEnvMain() {
    TestWithGbeWithEnvMain test = TestWithGbeWithEnvMain.createInstance();
    assertEquals(0, GuiceBerryEnvWithEnvMain.MyGuiceBerryEnvMain.count);
    GuiceBerryJunit3.setUp(test);
    assertEquals(1, GuiceBerryEnvWithEnvMain.MyGuiceBerryEnvMain.count);
  }
 
  public void testTearDownOnModuleNoTestScopeListenerBindingNoPreviousSetUp() {

    TestWithGbeThatDoesNotBindATestScopeListener test = 
      TestWithGbeThatDoesNotBindATestScopeListener.createInstance();
    
    try {
      GuiceBerryJunit3.tearDown(test);
      fail();
    } catch (RuntimeException expected) {}  
  }
   
  public void testTestScopeListenerGetsNotifiesThatTestEntersTheScope() {
    TestWithGbeTwo test = TestWithGbeTwo.createInstance();
   
    long baz = BazService.counter;
    
    assertNotNull(baz);
    GuiceBerryJunit3.setUp(test);
    assertNotNull(test.baz);
    long baz2 = test.baz.getCounter();
     
    assertTrue(baz < baz2);   
    
  }
 
  public void testTestScopeListenerGetsNotifiesThatTestExitsTheScope() {
    NonTdtcWithGbeTwo test = NonTdtcWithGbeTwo.createInstance();
    
    GuiceBerryJunit3.setUp(test);
    assertNotNull(test.baz);
    long baz = test.baz.getCounter();
    GuiceBerryJunit3.tearDown(test);
    long baz2 = test.baz.getCounter();
    assertTrue(baz < baz2);   
  }

  public void testTestScopeIsCreatedForModule() 
      throws ClassNotFoundException {
    TestWithGbeOne test = TestWithGbeOne.createInstance();
    
   assertNull(GuiceBerryJunit3.getTestScopeForGbe(
       Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    GuiceBerryJunit3.setUp(test);
    JunitTestScope testScope = 
      GuiceBerryJunit3.getTestScopeForGbe(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNotNull(testScope);
  }  
 
  public void testReUseTestScopeByTwoTestsWithSameGbe() 
    throws ClassNotFoundException{
    TestWithGbeOne testOne = TestWithGbeOne.createInstance();
    assertNull(
        GuiceBerryJunit3.getTestScopeForGbe(
            Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    GuiceBerryJunit3.setUp(testOne);
    JunitTestScope testScopeOne = 
      GuiceBerryJunit3.getTestScopeForGbe(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNotNull(testScopeOne);
    
    testOne.run();
   
    AnotherTestWithGbeOne testTwo = 
      AnotherTestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testTwo);
    assertNotNull(GuiceBerryJunit3.getTestScopeForGbe(
        Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    
    JunitTestScope testScopeTwo = 
      GuiceBerryJunit3.getTestScopeForGbe(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertSame(testScopeOne, testScopeTwo);
    assertEquals(1, GuiceBerryJunit3.numberOfInjectorsInUse());
  }  

  public void testThatTestsWithDifferentGbesGetDifferentTestScopes() 
      throws ClassNotFoundException {
    TestWithGbeOne testOne = TestWithGbeOne.createInstance();
   
    assertNull(GuiceBerryJunit3.getTestScopeForGbe(
        Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    GuiceBerryJunit3.setUp(testOne);
    JunitTestScope testScopeOne = 
      GuiceBerryJunit3.getTestScopeForGbe(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNotNull(testScopeOne);
  
    testOne.run();
    
    TestWithGbeTwo testTwo = TestWithGbeTwo.createInstance();
    GuiceBerryJunit3.setUp(testTwo);
    assertNotNull(GuiceBerryJunit3.getTestScopeForGbe(
        Class.forName(GuiceBerryEnvTwo.GUICE_BERRY_ENV_TWO)));
    JunitTestScope testScopeTwo = 
      GuiceBerryJunit3.getTestScopeForGbe(
          Class.forName(GuiceBerryEnvTwo.GUICE_BERRY_ENV_TWO));
    
    assertNotSame(testScopeOne, testScopeTwo); 
  }
  
  public void testInjectingTestCasesIntoTestScopeListeners() 
      throws Exception {
    TestWithGbeThatInjectsATestCaseIntoTestScopeListener test = 
      TestWithGbeThatInjectsATestCaseIntoTestScopeListener.createInstance();

    GuiceBerryJunit3.setUp(test);
    test.run();
  }

  public void testModuleThatFailsInjectorCreation() throws Throwable {
    TestWithGbeThatFailsInjectorCreation test =
        TestWithGbeThatFailsInjectorCreation.createInstance();

    // the first run should die on Injector-creation
    try {
      test.setUp();
      fail();
    } catch (RuntimeException expected) {
      String expectedMessage = 
        "Error injecting method, java.lang.UnsupportedOperationException: kaboom!";
      assertTrue(expected.getMessage().contains(expectedMessage));
    }
    
    // Even when setUp fails (in this case with CreationException), tearDown
    // will execute, and should not throw an Exception
    GuiceBerryJunit3.tearDown(test);
      
    try {
      test.setUp();
      fail();
    } catch (RuntimeException expected) {
      assertEquals(String.format(
          "Skipping '%s' GuiceBerryEnv which failed previously during injector creation.",
          GuiceBerryEnvThatFailsInjectorCreation.GUICE_BERRY_ENV_THAT_FAILS_INJECTOR_CREATION),
          expected.getMessage());
    }
  }

//THE BELOW CLASSES ARE USED ONLY FOR TESTING GuiceBerry
  
  public static final class MyGuiceBerryEnvRemapper 
      implements GuiceBerryEnvRemapper {
    public String remap(TestCase test, String guiceBerryEnv) {
      return GuiceBerryEnvTwo.class.getName();
    }
  }

  public static final class MyGuiceBerryEnvRemapperThatReturnsNull 
      implements GuiceBerryEnvRemapper {
    public String remap(TestCase test, String guiceBerryEnv) {
      return null;
    }
  }

  private static final class MyGuiceBerryEnvRemapperWithInvalidConstructor 
      implements GuiceBerryEnvRemapper {
    public MyGuiceBerryEnvRemapperWithInvalidConstructor(int foo) {}

    public String remap(TestCase test, String guiceBerryEnv) {
      return GuiceBerryEnvTwo.class.getName();
    }
  }

  private static final class MyNonGuiceBerryEnvRemapper {}
  
  @GuiceBerryEnv(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)
  private static final class TestWithGbeOne extends TearDownTestCase {
    
    @Inject
    private TestScopeListener testScopeListener;
    
    @Inject
    private BarService barService; 
    
    @Inject
    private FooService fooService;
    
    @Inject
    private TestId testId;

    @Inject
    private TestCase testCase; 
  
    static TestWithGbeOne createInstance() {
      TestWithGbeOne result = new TestWithGbeOne();
      result.setName("fooTest");
      return result;
    }    
  }
  
  @GuiceBerryEnv(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)
  private static final class AnotherTestWithGbeOne extends TearDownTestCase {
    
    @Inject
    private TestId testId;
    
    @Inject 
    private TestCase testCase;

    private static AnotherTestWithGbeOne createInstance() {
      return namedTest(new AnotherTestWithGbeOne());
    }
  }
  
  @GuiceBerryEnv(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)
  private static final class NonTdtcForGbeOne extends TestCase {

    @Inject
    private int number;

    @Inject
    private TestId testId;
    
    @Inject 
    private TestCase testCase;

    private static NonTdtcForGbeOne createInstance() {
      return namedTest(new NonTdtcForGbeOne());
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)
  private static final class AnotherNonTdtcForGbeOne extends TestCase {

    @Inject
    private int number;

    private static AnotherNonTdtcForGbeOne createInstance() {
      return namedTest(new AnotherNonTdtcForGbeOne());
    }  
  }
  
  @GuiceBerryEnv(GuiceBerryEnvTwo.GUICE_BERRY_ENV_TWO)
  private static final class TestWithGbeTwo extends TearDownTestCase {

    @Inject
    private TestScopeListener testScopeListener;  
    
    @Inject
    private int number;
 
    @Inject
    private BazService baz;
   
    private static TestWithGbeTwo createInstance() {
      return namedTest(new TestWithGbeTwo());
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvTwo.GUICE_BERRY_ENV_TWO)
  private static final class NonTdtcWithGbeTwo extends TestCase {

    @Inject
    private BazService baz;
   
    private static NonTdtcWithGbeTwo createInstance() {
      return namedTest(new NonTdtcWithGbeTwo());
    }  
  }
  
  @GuiceBerryEnv(GuiceBerryEnvWithEnvMain.GUICE_BERRY_ENV_WITH_ENV_MAIN)
  private static final class TestWithGbeWithEnvMain extends TearDownTestCase {

    private static TestWithGbeWithEnvMain createInstance() {
      return namedTest(new TestWithGbeWithEnvMain());
    }
  }

  @GuiceBerryEnv(GUICE_BERRY_ENV_THAT_DOES_NOT_EXIST)
  private static final class TestWithNonExistingGbe extends TearDownTestCase {
    
    private static TestWithNonExistingGbe createInstance() {
      return namedTest(new TestWithNonExistingGbe());
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvWithoutBindingsForFooOrBar.GUICE_BERRY_ENV_WITHOUT_BINDINGS_FOR_FOO_OR_BAR)
  private static final class TestWithGbeThatHasMissingBindings 
      extends TearDownTestCase {
    @SuppressWarnings("unused")
    @Inject
    private BarService barService; 
    
    private static TestWithGbeThatHasMissingBindings createInstance() {
      return namedTest(new TestWithGbeThatHasMissingBindings());
    }  
  }
  
  @GuiceBerryEnv(NotAGuiceBerryEnvOne.NOT_A_GUICE_BERRY_ENV_ONE)
  private static final class TestWithGbeThatNotImplementsModule 
      extends TearDownTestCase {
    
    private static TestWithGbeThatNotImplementsModule createInstance() {
      return namedTest(new TestWithGbeThatNotImplementsModule());
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvWithIllegalConstructor.GUICE_BERRY_ENV_WITH_ILLEGAL_CONSTRUCTOR)
  private static final class TestWithGbeThatHasAnIllegalConstructor
      extends TearDownTestCase {
    
    private static TestWithGbeThatHasAnIllegalConstructor createInstance() {
      return namedTest(new TestWithGbeThatHasAnIllegalConstructor());
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvWithNoTestScopeListener.GUICE_BERRY_ENV_WITH_NO_TEST_SCOPE_LISTENER)
  private static final class TestWithGbeThatDoesNotBindATestScopeListener 
      extends TearDownTestCase {
    
    private static TestWithGbeThatDoesNotBindATestScopeListener createInstance() {
      return namedTest(new TestWithGbeThatDoesNotBindATestScopeListener());
    }  
  }

  @GuiceBerryEnv(NOT_A_GUICE_BERRY_ENV_BECAUSE_IT_IS_ABSTRACT)
  private static final class TestWithGbeThatIsAnAbstractClass 
      extends TearDownTestCase {
    
    private static TestWithGbeThatIsAnAbstractClass createInstance() {
      return namedTest(new TestWithGbeThatIsAnAbstractClass());
    }  
  }
  
  @GuiceBerryEnv(GuiceBerryEnvWithNonTrivialTestScopeListener.MODULE_NAME_INJECTS_TEST_CASE_IN_TEST_SCOPE_LISTENER)
  private static final class TestWithGbeThatInjectsATestCaseIntoTestScopeListener
      extends TestCase {
    
    private static TestWithGbeThatInjectsATestCaseIntoTestScopeListener createInstance() {
      return namedTest(new TestWithGbeThatInjectsATestCaseIntoTestScopeListener());
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvThatFailsInjectorCreation.GUICE_BERRY_ENV_THAT_FAILS_INJECTOR_CREATION)
  public static final class TestWithGbeThatFailsInjectorCreation
      extends TestCase {
    private static TestWithGbeThatFailsInjectorCreation createInstance() {
      return namedTest(new TestWithGbeThatFailsInjectorCreation());
    }

    @Override
    protected void setUp() throws Exception {
      GuiceBerryJunit3.setUp(this);
    }

    public void testNothing() {}
  }
  
  public static final class UnAnnotatedNonTdtc extends TestCase {

    private static UnAnnotatedNonTdtc createInstance() {
      return namedTest(new UnAnnotatedNonTdtc());
    }
    
  }

// BELOW CLASSES IMPLEMENTS INTERFACE MODULE
// USED FOR GuiceBerryEnv ANNOTATIONS -- only for testing  
  
  private static int NUMBER = 0;
  
  public static class GuiceBerryEnvOne extends AbstractModule {
    private static final String GUICE_BERRY_ENV_ONE = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$" +
      		"GuiceBerryEnvOne";

    @Override
    public void configure() {
      install(new BasicJunit3Module());
      bind(BarService.class).to(BarServiceOne.class);
      bind(FooService.class).to(FooServiceOne.class);
      bind(Integer.class).toInstance(NUMBER++);
      bind(TestScopeListener.class).toInstance(new NoOpTestScopeListener());
    }
  }

  public static class GuiceBerryEnvTwo extends AbstractModule {
    private static final String GUICE_BERRY_ENV_TWO = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$" +
      		"GuiceBerryEnvTwo";

    @Override
    public void configure() {
      install(new BasicJunit3Module());
      bind(FooService.class).to(FooServiceTwo.class);
      bind(BarService.class).to(BarServiceTwo.class);      
      bind(Integer.class).toInstance(NUMBER++);
      bind(BazService.class).in(Singleton.class);
      bind(TestScopeListener.class).to(BazService.class).in(Scopes.SINGLETON);
    }
  }
  
  public static class GuiceBerryEnvWithEnvMain extends AbstractModule {
    private static final String GUICE_BERRY_ENV_WITH_ENV_MAIN = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$" +
      		"GuiceBerryEnvWithEnvMain";

    static final class MyGuiceBerryEnvMain implements GuiceBerryEnvMain {
      
      private static int count = 0;
      
      public void run() {
        count++;
      }
    }
    
    @Override
    public void configure() {
      install(new BasicJunit3Module());
      bind(FooService.class).to(FooServiceOne.class);
      bind(BarService.class).to(BarServiceOne.class);      
      bind(Integer.class).toInstance(NUMBER++);
      bind(TestScopeListener.class).toInstance(new NoOpTestScopeListener());
      bind(GuiceBerryEnvMain.class).to(MyGuiceBerryEnvMain.class);
    }
  }
  public static class GuiceBerryEnvWithoutBindingsForFooOrBar 
      extends AbstractModule  {
    private static final String GUICE_BERRY_ENV_WITHOUT_BINDINGS_FOR_FOO_OR_BAR =
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$" +
      		"GuiceBerryEnvWithoutBindingsForFooOrBar";

    @Override
    public void configure() {
      install(new BasicJunit3Module());
      bind(TestScopeListener.class).toInstance(new NoOpTestScopeListener());
    }
  }

  public static class GuiceBerryEnvWithNonTrivialTestScopeListener 
      extends AbstractModule {
    private static final String MODULE_NAME_INJECTS_TEST_CASE_IN_TEST_SCOPE_LISTENER = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$" +
      		"GuiceBerryEnvWithNonTrivialTestScopeListener";

    @Override
    public void configure() {
      install(new BasicJunit3Module());
      bind(TestScopeListener.class)
        .toInstance(new TestScopeListenerGetsInjectedWithTestCase());     
    }
  }
  
  
  public static class GuiceBerryEnvWithIllegalConstructor implements Module {    
    private static final String GUICE_BERRY_ENV_WITH_ILLEGAL_CONSTRUCTOR = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$" +
      		"GuiceBerryEnvWithIllegalConstructor";

    /**
     * Constructors should be no-args
     */
    public GuiceBerryEnvWithIllegalConstructor(int a){}
    
    public void configure(Binder binder) {}
  }

  public static class GuiceBerryEnvWithNoTestScopeListener 
      extends AbstractModule {    
    private static final String GUICE_BERRY_ENV_WITH_NO_TEST_SCOPE_LISTENER = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$" +
      		"GuiceBerryEnvWithNoTestScopeListener";

    @Override
    public void configure() {
      install(new BasicJunit3Module());
      bind(FooService.class).to(FooServiceTwo.class);
      bind(BarService.class).to(BarServiceTwo.class);      
      bind(Integer.class).toInstance(NUMBER++);
    }
  }   
  
  /**
   * {@link GuiceBerryEnv}s must be {@link Module}s.
   */
  public static class NotAGuiceBerryEnvOne {
    private static final String NOT_A_GUICE_BERRY_ENV_ONE = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$" +
      		"NotAGuiceBerryEnvOne"; 
  }

  public static class GuiceBerryEnvThatFailsInjectorCreation
      extends AbstractModule  {
    private static final String GUICE_BERRY_ENV_THAT_FAILS_INJECTOR_CREATION =
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$" +
      		"GuiceBerryEnvThatFailsInjectorCreation";

    @Override
    public void configure() {
      install(new BasicJunit3Module());
      requestStaticInjection(getClass());
    }

    @Inject static void throwAnExceptionPlease() {
      throw new UnsupportedOperationException("kaboom!");
    }
  }


//BELOW CLASSES ARE USED TO TEST IF GUICEBERRY BINDS THINGS PROPERLY   
// used only for testing
 
  private static class BazService implements TestScopeListener {
    private static long counter = 0;
    
    public void enteringScope() {
      counter++; 
    }

    public void exitingScope() {
      counter++; 
    }
    
    long getCounter(){
      return counter;
    }  
  }  
  
  private  interface FooService {
    public String get();
  }
    
  private static class TestScopeListenerGetsInjectedWithTestCase implements TestScopeListener {
    @Inject
    TestCase testCase;
    
    public void enteringScope() { }

    public void exitingScope() { }
  }
  
  private static class FooServiceTwo implements FooService {
    
    @Inject private String information;
    
    public String get(){
      return information;
    }
  }
     
  private static class FooServiceOne implements FooService { 
    @Inject private String information;
    
    public String get(){
      return information;
    }
  }
     
  private interface BarService {
    public TestId getTestId();
  }
     
  private static class BarServiceTwo implements BarService {
    @Inject 
    private TestId testId; 
    
    public TestId getTestId(){
      return testId;
    }
  }
         
  private static class BarServiceOne implements BarService {
    public TestId getTestId(){
      return null;
    }
  } 

  private static <T extends TestCase> T namedTest(T test) {
    test.setName(test.getClass().getCanonicalName());
    return test;
  }  
}
