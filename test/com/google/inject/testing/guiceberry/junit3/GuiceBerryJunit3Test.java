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
      TestWithNonExistingGbe testClass = 
        TestWithNonExistingGbe.createInstance();
      GuiceBerryJunit3.setUp(testClass);
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
      TestWithGbeThatHasMissingBindings testClass = 
        TestWithGbeThatHasMissingBindings.createInstance();
      GuiceBerryJunit3.setUp(testClass);
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
      TestWithGbeThatNotImplementsModule testClass = 
        TestWithGbeThatNotImplementsModule.createInstance();
      GuiceBerryJunit3.setUp(testClass);
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
      TestWithGbeThatHasAnIllegalConstructor testClass = 
        TestWithGbeThatHasAnIllegalConstructor.createInstance();
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("@GuiceBerryEnv class " +
              "'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$GuiceBerryEnvWithIllegalConstructor' " +
              "must have a public zero-arguments constructor", 
              expected.getMessage());
    }
  }
 
  public void testGbeIsAbstractClassModuleThrowsException() {
    TestWithGbeThatIsAnAbstractClass testClass = 
      TestWithGbeThatIsAnAbstractClass.createInstance();
    try {
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (RuntimeException expected) {
      assertTrue(expected.getCause() instanceof InstantiationException);
    }
  }
  
  public void testSimpleValidGbe() {
    TestWithGbeOne testClass = TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testClass);
    assertNotNull(testClass.barService);
    assertNotNull(testClass.fooService);
  }

  private static class MyThread extends Thread {
    private TestCase theTestCase;

    @Override
    public void run() {
      theTestCase = GuiceBerryJunit3.getActualTestCase();
    }
  }
  
  public void testThatTestScopeThreadLocalInherits() throws InterruptedException {
    TestWithGbeOne testClass = TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testClass);

    // Finding the current test case from a secondary thread.
    MyThread myThread = new MyThread();
    myThread.start();   
    myThread.join();
    
    assertNotNull(myThread.theTestCase);
    assertEquals(TestWithGbeOne.class.getName(), 
        myThread.theTestCase.getClass().getName());
  }

  public void testThatTestCaseGetsInjectedWithWhatsConfiguredInTheGbe() {
    TestWithGbeOne testClass = TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testClass);
    assertTrue(testClass.barService instanceof BarServiceOne);
    assertTrue(testClass.fooService instanceof FooServiceOne);
  }
  
  public void testInjectorMapIsSetAfterATest() throws ClassNotFoundException {
    TestWithGbeOne firstTest = TestWithGbeOne.createInstance();
    Injector injector = GuiceBerryJunit3.getInjectorFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNull(injector);
    
    GuiceBerryJunit3.setUp(firstTest);
    injector = GuiceBerryJunit3.getInjectorFromGB(
        Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    
    assertNotNull(injector);
  }
  
  public void testThatTwoTestsWithSameGbeUseTheSameInjector() 
      throws ClassNotFoundException {
    TestWithGbeOne firstTest = 
      TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(firstTest);
    
    Injector firstInjector = 
      GuiceBerryJunit3.getInjectorFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    GuiceBerryJunit3.tearDown(firstTest);

    AnotherTestWithGbeOne secondTest = 
      AnotherTestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(secondTest);
    
    Injector secondInjector = 
      GuiceBerryJunit3.getInjectorFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));

    // "number" is bound to a random, so this will only pass if the injector
    // used for both tests was the same
    assertEquals(firstTest.number, secondTest.number);
    // This would fail if the first injector was, say, thrown away and replaced
    // when running the second test
    assertSame(firstInjector, secondInjector);    
    // This would fail if GuiceBerryJunit3 creates another injector for the
    // benefit of the second test
    assertEquals(1, GuiceBerryJunit3.numberOfInjectorsInUse());    
  }
  
  public void testNotReUsingInjectorForTestsThatDeclaresADifferentGbe() {
    TestWithGbeOne firstTest = 
      TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(firstTest);    
    GuiceBerryJunit3.tearDown(firstTest);
    
    TestWithGbeTwo secondTest = 
      TestWithGbeTwo.createInstance();
    GuiceBerryJunit3.setUp(secondTest);
    JUnitAsserts.assertNotEqual(firstTest.number, secondTest.number);
  }

  public void testPutTwoInjectorsInMapForTestsThatDeclareDifferentGbes() {
    TestWithGbeOne firstTest = 
      TestWithGbeOne.createInstance();
    
    GuiceBerryJunit3.setUp(firstTest);
    GuiceBerryJunit3.tearDown(firstTest);
    
    TestWithGbeTwo secondTest = 
      TestWithGbeTwo.createInstance();
    
    GuiceBerryJunit3.setUp(secondTest);
   
    assertEquals(2, GuiceBerryJunit3.numberOfInjectorsInUse());    
  }
  
  public void testRemapper() {
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
      MyGuiceBerryEnvRemapper.class.getName());

    GuiceBerryJunit3.setUp(testClass);
    assertEquals(BarServiceTwo.class, testClass.barService.getClass());
    assertEquals(FooServiceTwo.class, testClass.fooService.getClass());
  }
  
  public void testRemapperThatReturnsNullGivesGoodErrorMessage() {
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
      MyGuiceBerryEnvRemapperThatReturnsNull.class.getName());

    try {
      GuiceBerryJunit3.setUp(testClass);
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
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
      "foo");

    try {
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("Class 'foo', which is being declared as a GuiceBerryEnvRemapper, does not exist.", 
        expected.getMessage());
    }
  }
  
  public void testRemapperSystemPropertyNeedsClassThatImplementsCorrectInterface() {
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
          MyNonGuiceBerryEnvRemapper.class.getName());

    try {
      GuiceBerryJunit3.setUp(testClass);
    fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("Class 'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$MyNonGuiceBerryEnvRemapper' " +
        "is being declared as a GuiceBerryEnvRemapper, but does not implement that interface", 
        expected.getMessage());
    }
  }

  public void testRemapperSystemPropertyNeedsClassWithZeroArgConstructor() {
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
      MyGuiceBerryEnvRemapperWithInvalidConstructor.class.getName());

    try {
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("GuiceBerryEnvRemapper 'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$MyGuiceBerryEnvRemapperWithInvalidConstructor' " +
        "must have public zero-arguments constructor", expected.getMessage());
    }
  }

  public void testInjectionOfTestId() {
    TestWithGbeOne testClass =  
      TestWithGbeOne.createInstance();
    
    assertNull(testClass.testId);
    GuiceBerryJunit3.setUp(testClass);
    assertNotNull(testClass.testId);

    TestId expectedTestId = new TestId(testClass, testClass.testId.random);

    assertEquals(expectedTestId, testClass.testId);
  } 
  
  public void testDifferentTestsGetInjectedWithDifferentTestIds() {
    
    TestWithGbeOne firstTest =  
      TestWithGbeOne.createInstance();
    
    GuiceBerryJunit3.setUp(firstTest);
    
    assertEquals(
        new TestId(firstTest, 
            firstTest.testId.random),
        firstTest.testId);
    GuiceBerryJunit3.tearDown(firstTest);
   
    AnotherTestWithGbeOne secondTest = 
      AnotherTestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(secondTest);
   
    assertEquals(
        new TestId(secondTest, secondTest.testId.random),
        secondTest.testId);
  }
  
  public void testInjectionOfTestCase() {  
    TestWithGbeOne testClass =  
      TestWithGbeOne.createInstance();
    
    assertEquals(null, testClass.testCase);
    GuiceBerryJunit3.setUp(testClass);
    assertEquals(testClass.getName(), testClass.testCase.getName());
  } 
  
  
  public void testDifferentTestsGetsInjectedWithDifferentTestCases() {
    
    TestWithGbeOne firstTest =  
      TestWithGbeOne.createInstance();
    
    GuiceBerryJunit3.setUp(firstTest);
    
    assertEquals(firstTest.getName(), firstTest.testCase.getName());
    
    GuiceBerryJunit3.tearDown(firstTest);
   
    AnotherTestWithGbeOne secondTest = 
      AnotherTestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(secondTest);
    
    assertEquals(secondTest.getName(), secondTest.testCase.getName());
    
    JUnitAsserts.assertNotEqual(firstTest.testCase, secondTest.testCase);
  }

  public void testMethodTearDownWorksProperly() {
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();
    
    GuiceBerryJunit3.setUp(testClass);
    assertEquals(testClass,  GuiceBerryJunit3.getActualTestCase());
    GuiceBerryJunit3.tearDown(testClass); 
  //No concurrence problems as the actual TestCase is: ThreadLocal<TestCase>
    assertNull(GuiceBerryJunit3.getActualTestCase());
  }
  
  public void testMethodTearDownNoPreviousSetupOnClassWithNoAnnotation() {
    try {
      GuiceBerryJunit3.tearDown(this);
      fail();
    } catch (NullPointerException expected) {}
  }
  
  
  public void testMethodTearDownNoPreviousSetupOnClassWithAnnotation() {
    
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();
    try {
      GuiceBerryJunit3.tearDown(testClass);
      fail();
    } catch (RuntimeException expected) {}  
  }
  
  
  public void testTearDownNoPreviousSetupOnClassWithAnnotationThatWasUsed() {

    TestWithGbeOne testClass1 = 
      TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testClass1);
    testClass1.run();
    TestWithGbeOne testClass2 = 
      TestWithGbeOne.createInstance();
    try {
      GuiceBerryJunit3.tearDown(testClass2);
      fail();
    } catch (RuntimeException expected) {}
  }
  
  
  public void testTearDownOnDifferentClassThatSetupWasCalled() {

    TestWithGbeOne testClass1 = 
      TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testClass1);
    AnotherTestWithGbeOne testClass2 = 
      AnotherTestWithGbeOne.createInstance();
    try {
      GuiceBerryJunit3.tearDown(testClass2);
      fail();
    } catch (RuntimeException expected) {}
  }
  
 
  public void testCallingTwoSetupWithNoTearDownBetween() {
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();
    try {
      GuiceBerryJunit3.setUp(testClass);
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (RuntimeException expected) {}   
  }
  
  public void testAddTearDownToTearDownTestCase() {
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();
   
    GuiceBerryJunit3.setUp(testClass);
    assertEquals(testClass, testClass.testCase);
    testClass.run();
  //No concurrence problems as the actual TestCase is: ThreadLocal<TestCase>
    assertNull(GuiceBerryJunit3.getActualTestCase());
  }

  public void testTestCaseCanBeUsedInsteadOfTearDownTestCase() {
    NonTdtcForGbeOne testClass = 
      NonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testClass);
    testClass.run();     
  }
  
  public void testMethodTearDownForTestCaseNotCalledAutomatically() {
    NonTdtcForGbeOne testClass = 
      NonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testClass);
    testClass.run();

    TestWithGbeOne testClass2 = 
      TestWithGbeOne.createInstance();
    try {  
      GuiceBerryJunit3.setUp(testClass2);
      fail();
    } catch (RuntimeException expected) {}
  }
 
  public void testMethodTearDownForTestCaseCalledManually() {
    NonTdtcForGbeOne testClass = 
      NonTdtcForGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testClass);   
    testClass.run();
 
    GuiceBerryJunit3.tearDown(testClass);
    TestWithGbeOne testClass2 = 
      TestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testClass2);
    testClass2.run();
  }   
 
  public void testGbeThaHasNoTestScopeListenerBinding() {
    TestWithGbeThatDoesNotBindATestScopeListener testClass = 
      TestWithGbeThatDoesNotBindATestScopeListener.createInstance();
    try {
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (RuntimeException expected) {
      assertEquals("TestScopeListener must be bound in your GuiceBerryEnv.", 
          expected.getMessage());
    }  
  }
  
  public void testGbeThatBindsTestScopeListenerToNoOpTestScopeListener() 
      throws ClassNotFoundException {
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();

    GuiceBerryJunit3.setUp(testClass);
    TestScopeListener scopeListener =
      GuiceBerryJunit3.getInjectorFromGB(
      Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE))
      .getInstance(TestScopeListener.class);
    
    assertTrue(scopeListener instanceof NoOpTestScopeListener);    
  }
  
  public void testGbeWithCustomTestScopeListener() 
      throws ClassNotFoundException {
    TestWithGbeTwo testClass1 = 
      TestWithGbeTwo.createInstance();
      
      GuiceBerryJunit3.setUp(testClass1);
      TestScopeListener scopeListener =
        GuiceBerryJunit3.getInjectorFromGB(
        Class.forName(GuiceBerryEnvTwo.GUICE_BERRY_ENV_TWO))
        .getInstance(TestScopeListener.class);
      
      assertTrue(scopeListener instanceof BazService);    
  }
 
  public void testTearDownOnModuleNoTestScopeListenerBindingNoPreviousSetUp() {

    TestWithGbeThatDoesNotBindATestScopeListener testClass = 
      TestWithGbeThatDoesNotBindATestScopeListener.createInstance();
    
    try {
      GuiceBerryJunit3.tearDown(testClass);
      fail();
    } catch (RuntimeException expected) {}  
  }
   
  public void testTestScopeListenerGetsNotifiesThatTestEntersTheScope() {
    TestWithGbeTwo testClass = 
      TestWithGbeTwo.createInstance();
   
    long baz = BazService.counter;
    
    assertNotNull(baz);
    GuiceBerryJunit3.setUp(testClass);
    assertNotNull(testClass.baz);
    long baz2 = testClass.baz.getCounter();
     
    assertTrue(baz < baz2);   
    
  }
 
  public void testTestScopeListenerGetsNotifiesThatTestExitsTheScope() {
    TestWithGbeTwo testClass = 
      TestWithGbeTwo.createInstance();
    
    GuiceBerryJunit3.setUp(testClass);
    assertNotNull(testClass.baz);
    long baz = testClass.baz.getCounter();
    GuiceBerryJunit3.tearDown(testClass);
    long baz2 = testClass.baz.getCounter();
    assertTrue(baz < baz2);   
  }

  public void testTestScopeIsCreatedForModule() 
      throws ClassNotFoundException {
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();
    
   assertNull(GuiceBerryJunit3.getTestScopeFromGB(
       Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    GuiceBerryJunit3.setUp(testClass);
    JunitTestScope testScope = 
      GuiceBerryJunit3.getTestScopeFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNotNull(testScope);
  }  
 
  public void testReUseTestScopeByTwoTestsWithSameGbe() 
    throws ClassNotFoundException{
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();
    assertNull(
        GuiceBerryJunit3.getTestScopeFromGB(
            Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    GuiceBerryJunit3.setUp(testClass);
    JunitTestScope testScope = 
      GuiceBerryJunit3.getTestScopeFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNotNull(testScope);
    
    testClass.run();
   
    AnotherTestWithGbeOne testClass2 = 
      AnotherTestWithGbeOne.createInstance();
    GuiceBerryJunit3.setUp(testClass2);
    assertNotNull(GuiceBerryJunit3.getTestScopeFromGB(
        Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    
    JunitTestScope testScope2 = 
      GuiceBerryJunit3.getTestScopeFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertSame(testScope, testScope2);
    assertEquals(1, GuiceBerryJunit3.numberOfInjectorsInUse());
  }  

  public void testThatTestsWithDifferentGbesGetDifferentTestScopes() 
      throws ClassNotFoundException {
    TestWithGbeOne testClass = 
      TestWithGbeOne.createInstance();
   
    assertNull(GuiceBerryJunit3.getTestScopeFromGB(
        Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    GuiceBerryJunit3.setUp(testClass);
    JunitTestScope testScope = 
      GuiceBerryJunit3.getTestScopeFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNotNull(testScope);
  
    testClass.run();
    
    TestWithGbeTwo testClass2 = 
      TestWithGbeTwo.createInstance();
    GuiceBerryJunit3.setUp(testClass2);
    assertNotNull(GuiceBerryJunit3.getTestScopeFromGB(
        Class.forName(GuiceBerryEnvTwo.GUICE_BERRY_ENV_TWO)));
    JunitTestScope testScope2 = 
      GuiceBerryJunit3.getTestScopeFromGB(
          Class.forName(GuiceBerryEnvTwo.GUICE_BERRY_ENV_TWO));
    
    assertNotSame(testScope, testScope2); 
  }
  
  public void testInjectingTestCasesIntoTestScopeListeners() 
      throws Exception {
    TestWithGbeThatInjectsATestCaseIntoTestScopeListener testClass = 
      TestWithGbeThatInjectsATestCaseIntoTestScopeListener.createInstance();

    GuiceBerryJunit3.setUp(testClass);
    testClass.run();
  }

  public void testModuleThatFailsInjectorCreation() throws Throwable {
    TestWithGbeThatFailsInjectorCreation testCase =
        TestWithGbeThatFailsInjectorCreation.createInstance();

    // the first run should die on Injector-creation
    try {
      testCase.setUp();
      fail();
    } catch (RuntimeException expected) {
      String expectedMessage = 
        "Error injecting method, java.lang.UnsupportedOperationException: kaboom!";
      assertTrue(expected.getMessage().contains(expectedMessage));
    }
    
    // Even when setUp fails (in this case with CreationException), tearDown
    // will execute, and should not throw an Exception
    GuiceBerryJunit3.tearDown(testCase);
      
    try {
      testCase.setUp();
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
    private BarService barService; 
    
    @Inject
    private FooService fooService;
    
    @Inject
    private int number;
    
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
    private int number;
    
    @Inject
    private TestId testId;
    
    @Inject 
    private TestCase testCase;

    static AnotherTestWithGbeOne createInstance() {
      AnotherTestWithGbeOne result = new AnotherTestWithGbeOne();
      result.setName(AnotherTestWithGbeOne.class.getCanonicalName()); 
      return result;
    }
  }
  
  @GuiceBerryEnv(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)
  private static final class NonTdtcForGbeOne extends TestCase {
    @Inject
    TestCase testCase;
    
    static NonTdtcForGbeOne createInstance() {
      NonTdtcForGbeOne result = new NonTdtcForGbeOne();
      result.setName(NonTdtcForGbeOne.class.getCanonicalName());
      return result;
    }  
  }
  
  @GuiceBerryEnv(GuiceBerryEnvTwo.GUICE_BERRY_ENV_TWO)
  private static final class TestWithGbeTwo extends TearDownTestCase {

    @Inject
    private BarService barService;  
    
    @Inject
    private int number;
 
    @Inject
    private BazService baz;
   
    static TestWithGbeTwo createInstance() {
      TestWithGbeTwo result = new TestWithGbeTwo();
      result.setName(TestWithGbeTwo.class.getCanonicalName());
      return result;
    }  
  }
  
  @GuiceBerryEnv(GUICE_BERRY_ENV_THAT_DOES_NOT_EXIST)
  private static final class TestWithNonExistingGbe 
      extends TearDownTestCase {
    static TestWithNonExistingGbe createInstance() {
      TestWithNonExistingGbe result = new TestWithNonExistingGbe();
      result.setName(TestWithNonExistingGbe.class.getCanonicalName());
      return result;
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvWithoutBindingsForFooOrBar.GUICE_BERRY_ENV_WITHOUT_BINDINGS_FOR_FOO_OR_BAR)
  private static final class TestWithGbeThatHasMissingBindings 
      extends TearDownTestCase {
    @Inject
    BarService barService; 
    
    static TestWithGbeThatHasMissingBindings createInstance() {
      TestWithGbeThatHasMissingBindings result = 
        new TestWithGbeThatHasMissingBindings();
      result.setName(TestWithGbeThatHasMissingBindings
          .class.getCanonicalName());
      return result;
    }  
  }
  
  @GuiceBerryEnv(NotAGuiceBerryEnvOne.NOT_A_GUICE_BERRY_ENV_ONE)
  private static final class TestWithGbeThatNotImplementsModule 
      extends TearDownTestCase {
    
    static TestWithGbeThatNotImplementsModule createInstance() {
      TestWithGbeThatNotImplementsModule result = 
        new TestWithGbeThatNotImplementsModule();
      result.setName(TestWithGbeThatNotImplementsModule
          .class.getCanonicalName());
      return result;
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvWithIllegalConstructor.GUICE_BERRY_ENV_WITH_ILLEGAL_CONSTRUCTOR)
  private static final class TestWithGbeThatHasAnIllegalConstructor
    extends TearDownTestCase {
    
    static TestWithGbeThatHasAnIllegalConstructor createInstance() {
      TestWithGbeThatHasAnIllegalConstructor result = 
        new TestWithGbeThatHasAnIllegalConstructor();
      result.setName(TestWithGbeThatHasAnIllegalConstructor
          .class.getCanonicalName());
      return result;
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvWithNoTestScopeListener.GUICE_BERRY_ENV_WITH_NO_TEST_SCOPE_LISTENER)
  private static final class TestWithGbeThatDoesNotBindATestScopeListener 
      extends TearDownTestCase {
    
    static TestWithGbeThatDoesNotBindATestScopeListener createInstance() {
      TestWithGbeThatDoesNotBindATestScopeListener result = 
        new TestWithGbeThatDoesNotBindATestScopeListener();
      result.setName(TestWithGbeThatDoesNotBindATestScopeListener
          .class.getCanonicalName());
      return result;
    }  
  }

  @GuiceBerryEnv(NOT_A_GUICE_BERRY_ENV_BECAUSE_IT_IS_ABSTRACT)
  private static final class TestWithGbeThatIsAnAbstractClass 
      extends TearDownTestCase {
    
    static TestWithGbeThatIsAnAbstractClass createInstance() {
      TestWithGbeThatIsAnAbstractClass result = 
        new TestWithGbeThatIsAnAbstractClass();
      result.setName(TestWithGbeThatIsAnAbstractClass
          .class.getCanonicalName());
      return result;
    }  
  }
  
  @GuiceBerryEnv(GuiceBerryEnvWithNonTrivialTestScopeListener.MODULE_NAME_INJECTS_TEST_CASE_IN_TEST_SCOPE_LISTENER)
  private static final class TestWithGbeThatInjectsATestCaseIntoTestScopeListener
      extends TestCase {
    
    static TestWithGbeThatInjectsATestCaseIntoTestScopeListener createInstance() {
      TestWithGbeThatInjectsATestCaseIntoTestScopeListener result = 
        new TestWithGbeThatInjectsATestCaseIntoTestScopeListener();
      result.setName(TestWithGbeThatInjectsATestCaseIntoTestScopeListener.class
          .getCanonicalName());
      return result;
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvThatFailsInjectorCreation.GUICE_BERRY_ENV_THAT_FAILS_INJECTOR_CREATION)
  public static final class TestWithGbeThatFailsInjectorCreation
      extends TestCase {
    static TestWithGbeThatFailsInjectorCreation createInstance() {
      TestWithGbeThatFailsInjectorCreation result =
          new TestWithGbeThatFailsInjectorCreation();
      result.setName("testNothing");
      return result;
    }

    @Override
    protected void setUp() throws Exception {
      GuiceBerryJunit3.setUp(this);
    }

    public void testNothing() {}
    
  }

// BELOW CLASSES IMPLEMENTS INTERFACE MODULE
// USED FOR GuiceBerryEnv ANNOTATIONS -- only for testing  
  
  private static int NUMBER = 0;
  
  public static class GuiceBerryEnvOne extends AbstractModule {
    private static final String GUICE_BERRY_ENV_ONE = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$GuiceBerryEnvOne";

    @Override
    public void configure() {
      install(new BasicJunit3Module());
      bind(BarService.class).to(BarServiceOne.class);
      bind(FooService.class).to(FooServiceOne.class);
      bind(Integer.class).toInstance(NUMBER++);
      bind(TestScopeListener.class).toInstance(new NoOpTestScopeListener());
    }
  }

  /**
   * List GbeOne but binds a TestScopeListener and a GuiceBerryEnvMain
   */
  public static class GuiceBerryEnvTwo extends AbstractModule {
    private static final String GUICE_BERRY_ENV_TWO = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$GuiceBerryEnvTwo";

    private static final class MyGuiceBerryEnvMain implements GuiceBerryEnvMain {
      
      private int count = 0;
      
      public void main() {
        count++;
      }
    }
    
    @Override
    public void configure() {
      install(new BasicJunit3Module());
      bind(FooService.class).to(FooServiceTwo.class);
      bind(BarService.class).to(BarServiceTwo.class);      
      bind(Integer.class).toInstance(NUMBER++);
      bind(BazService.class).in(Singleton.class);
      bind(TestScopeListener.class).to(BazService.class).in(Scopes.SINGLETON);
      bind(GuiceBerryEnvMain.class).to(MyGuiceBerryEnvMain.class);
    }
  }

  public static class GuiceBerryEnvWithoutBindingsForFooOrBar 
      extends AbstractModule  {
    private static final String GUICE_BERRY_ENV_WITHOUT_BINDINGS_FOR_FOO_OR_BAR =
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$GuiceBerryEnvWithoutBindingsForFooOrBar";

    @Override
    public void configure() {
      install(new BasicJunit3Module());
      bind(TestScopeListener.class).toInstance(new NoOpTestScopeListener());
    }
  }

  public static class GuiceBerryEnvWithNonTrivialTestScopeListener 
      extends AbstractModule {
    private static final String MODULE_NAME_INJECTS_TEST_CASE_IN_TEST_SCOPE_LISTENER = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$GuiceBerryEnvWithNonTrivialTestScopeListener";

    @Override
    public void configure() {
      install(new BasicJunit3Module());
      bind(TestScopeListener.class)
        .toInstance(new TestScopeListenerGetsInjectedWithTestCase());     
    }
  }
  
  
  public static class GuiceBerryEnvWithIllegalConstructor implements Module {    
    private static final String GUICE_BERRY_ENV_WITH_ILLEGAL_CONSTRUCTOR = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$GuiceBerryEnvWithIllegalConstructor";

    /**
     * Constructors should be no-args
     */
    public GuiceBerryEnvWithIllegalConstructor(int a){}
    
    public void configure(Binder binder) {}
  }

  public static class GuiceBerryEnvWithNoTestScopeListener 
      extends AbstractModule {    
    private static final String GUICE_BERRY_ENV_WITH_NO_TEST_SCOPE_LISTENER = 
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$GuiceBerryEnvWithNoTestScopeListener";

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
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$NotAGuiceBerryEnvOne"; 
  }

  public static class GuiceBerryEnvThatFailsInjectorCreation
      extends AbstractModule  {
    private static final String GUICE_BERRY_ENV_THAT_FAILS_INJECTOR_CREATION =
      GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$GuiceBerryEnvThatFailsInjectorCreation";

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
}
