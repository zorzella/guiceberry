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

import junit.framework.TestCase;

import com.google.common.base.Objects;
import com.google.common.testing.TearDown;
import com.google.common.testing.junit3.JUnitAsserts;
import com.google.common.testing.junit3.TearDownTestCase;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.TestScopeListener;

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
    
  private static final String INJECTED_INFORMATION = "Injected information";  

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    TearDown tearDown = new TearDown() {
      public void tearDown() throws Exception {    
        GuiceBerryJunit3.clear();
      }
    };
    addRequiredTearDown(tearDown);
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
              "must have an @GuiceBerryModule annotation.", 
              expected.getMessage());
    }
  }
  
  public void testAnnotationWithModuleThatNotExistsThrowsException() {
   try {
      TestAnnotatedWithModuleThatNotExist testClass = 
        TestAnnotatedWithModuleThatNotExist.createInstance();
      GuiceBerryJunit3.setUp(testClass);
      fail();
   } catch (IllegalArgumentException expected) { 
       assertEquals(
               "@GuiceBerryModule class " +
               "'com.this.guice.berry.env.does.NotExist' " +
               "was not found.", 
               expected.getMessage());
   }
  }
 
  public void testAnnotationWithModuleThatHasMissingBindingsThrowsException() {   
    try {
      TestAnnotatedWithModuleThatHasMissingBindings testClass = 
        TestAnnotatedWithModuleThatHasMissingBindings.createInstance();
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (RuntimeException expected) {
      //TODO: we should assert expected's cause is ConfigurationException, but 
      //that exception is private
      assertTrue(expected.getMessage().startsWith("Binding error in the module"));
      String configurationExceptionSuffix = "Binding to " +
        BarService.class.getName() +
        " not found. No bindings to that type were found.";
      assertTrue(expected.getCause().getMessage().endsWith(configurationExceptionSuffix));
    }
  }
 
  public void testAnnotationWithClassThatNotImplementsModuleThrowsException() {
    try {
      TestAnnotatedWithClassThatNotImplementsModule testClass = 
        TestAnnotatedWithClassThatNotImplementsModule.createInstance();
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (IllegalArgumentException expected) {
        assertEquals("@GuiceBerryModule class " +
                "'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$NotAGuiceBerryEnvOne' " +
                "must be a Guice Module (i.e. implement com.google.inject.Module).", 
                expected.getMessage());
    }
  }
     
  public void testAnnotationWithClassThatHasWrongConstructorThrowsException() {
    try {
      TestAnnotatedWithModuleThatHAsWrongConstructor testClass = 
        TestAnnotatedWithModuleThatHAsWrongConstructor.createInstance();
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("@GuiceBerryModule class " +
              "'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$GuiceBerryEnvWithIllegalConstructor' " +
              "must have a public zero-arguments constructor", 
              expected.getMessage());
    }
  }
 
  public void testAnotatedClassWithAbstractClassModuleThrowsException() {
    TestAnnotatedWithModuleThatIsAbstractClass testClass = 
      TestAnnotatedWithModuleThatIsAbstractClass.createInstance();
    try {
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (RuntimeException expected) {
      assertTrue(expected.getCause() instanceof InstantiationException);
    }
  }
  
  public void testAnnotaionWithAbstractModuleThatImplementsOtherInterfaces() {      
    TestAnnotatedWithStubService1 testClass =
      TestAnnotatedWithStubService1.createInstance();    
    GuiceBerryJunit3.setUp(testClass);    
  }
  
  public void testThatTestCaseGetsInjectedWithSomething() {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();
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
  
  public void testThatThreadLocalsInherit() throws InterruptedException {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();
    GuiceBerryJunit3.setUp(testClass);

    // Finding the current test case from a secondary thread.
    MyThread myThread = new MyThread();
    myThread.start();   
    myThread.join();
    
    assertNotNull(myThread.theTestCase);
    assertEquals(TestAnnotatedWithStubService1.class.getName(), 
        myThread.theTestCase.getClass().getName());

  }

  public void testThatTestCaseGetsInjectedWithWhatsConfiguredInTheModule() {
   TestAnnotatedWithStubService1 testClass = 
     TestAnnotatedWithStubService1.createInstance();
   GuiceBerryJunit3.setUp(testClass);
   assertTrue(testClass.barService instanceof BarServiceOne);
   assertTrue(testClass.fooService instanceof FooServiceOne);
  }
  
  public void testInjectorMapIsSetAfterATest() throws ClassNotFoundException {
    TestAnnotatedWithStubService1 firstTest = 
      TestAnnotatedWithStubService1.createInstance();
    Injector injector = GuiceBerryJunit3.getInjectorFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNull(injector);
    
    GuiceBerryJunit3.setUp(firstTest);
    injector = GuiceBerryJunit3.getInjectorFromGB(
        Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    
    assertNotNull(injector);
  }
  
  public void testNumberOfInjectorsNotChangesForTestCaseThatDeclaresSameModule() 
      throws ClassNotFoundException {
    TestAnnotatedWithStubService1 firstTest = 
      TestAnnotatedWithStubService1.createInstance();
    GuiceBerryJunit3.setUp(firstTest);
    
    Injector firstInjector = 
      GuiceBerryJunit3.getInjectorFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    GuiceBerryJunit3.tearDown(firstTest);

    TestAnnotatedWithStubService2 secondTest = 
      TestAnnotatedWithStubService2.createInstance();
    GuiceBerryJunit3.setUp(secondTest);
    
    Injector secondInjector = 
      GuiceBerryJunit3.getInjectorFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    
    assertSame(firstInjector, secondInjector);    
    assertEquals(1, GuiceBerryJunit3.numberOfInjectorsInUse());    
  }
  
  public void testReUseingInjectorForTestCasesThatDeclaresSameModuleName() {
    
    TestAnnotatedWithStubService1 firstTest = 
      TestAnnotatedWithStubService1.createInstance();
    GuiceBerryJunit3.setUp(firstTest);    
    GuiceBerryJunit3.tearDown(firstTest);
    
    TestAnnotatedWithStubService2 secondTest = 
      TestAnnotatedWithStubService2.createInstance();
    GuiceBerryJunit3.setUp(secondTest);
    assertEquals(firstTest.number, secondTest.number);

  }

  public void testNotReUseingInjectorForTestsThatDeclaresDifferentModules() {
    
    TestAnnotatedWithStubService1 firstTest = 
      TestAnnotatedWithStubService1.createInstance();
    GuiceBerryJunit3.setUp(firstTest);    
    GuiceBerryJunit3.tearDown(firstTest);
    
    TestAnnotatedWithRealService secondTest = 
      TestAnnotatedWithRealService.createInstance();
    GuiceBerryJunit3.setUp(secondTest);
    JUnitAsserts.assertNotEqual(firstTest.number, secondTest.number);

  }

  public void testPutTwoInjectorsInMapForTestsThatDeclareDifferentModules() {
   
    TestAnnotatedWithStubService1 firstTest = 
      TestAnnotatedWithStubService1.createInstance();
    
    GuiceBerryJunit3.setUp(firstTest);
    GuiceBerryJunit3.tearDown(firstTest);
    
    TestAnnotatedWithRealService secondTest = 
      TestAnnotatedWithRealService.createInstance();
    
    GuiceBerryJunit3.setUp(secondTest);
   
    assertEquals(2, GuiceBerryJunit3.numberOfInjectorsInUse());    
   }
  
  public void testCreateingCascadingInjections() {
    
    TestAnnotatedWithRealService testClass = 
      TestAnnotatedWithRealService.createInstance();
    GuiceBerryJunit3.setUp(testClass);       
    assertEquals(INJECTED_INFORMATION, testClass.fooService.get());
   }
  
  public void testSimpleOverrideSystemPropertyOverridesModule() {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();
    
    TearDown tearDown = new TearDown() {
    
      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryJunit3
            .buildModuleOverrideProperty(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
      }
    };
    addRequiredTearDown(tearDown);
    System.setProperty(GuiceBerryJunit3
        .buildModuleOverrideProperty(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE), 
        GuiceBerryEnvTwo.GUICE_BERRY_ENV_TWO);
    
    GuiceBerryJunit3.setUp(testClass);
    assertEquals(BarServiceTwo.class, testClass.barService.getClass());
    assertEquals(FooServiceTwo.class, testClass.fooService.getClass());
  }

  public void testRemapperSystemPropertyOverridesModule() {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addRequiredTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
      MyGuiceBerryEnvRemapper.class.getName());

    GuiceBerryJunit3.setUp(testClass);
    assertEquals(BarServiceTwo.class, testClass.barService.getClass());
    assertEquals(FooServiceTwo.class, testClass.fooService.getClass());
  }
  
  public void testRemapperSystemPropertyNeedsClassThatExists() {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addRequiredTearDown(tearDown);
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
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addRequiredTearDown(tearDown);
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
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();

    TearDown tearDown = new TearDown() {

      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
      }
    };
    addRequiredTearDown(tearDown);
    System.setProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME, 
      MyInvalidGuiceBerryEnvRemapper.class.getName());

    try {
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("GuiceBerryEnvRemapper 'com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3Test$MyInvalidGuiceBerryEnvRemapper' " +
        "must have public zero-arguments constructor", expected.getMessage());
    }
  }

  public void testNotExistingModuldeOverridesModuleThrowsException() {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();
    
    TearDown tearDown = new TearDown() {
    
      public void tearDown() throws Exception {
        System.clearProperty(GuiceBerryJunit3
            .buildModuleOverrideProperty(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
      }
    };
    
    addRequiredTearDown(tearDown);
    
    System.setProperty(GuiceBerryJunit3
        .buildModuleOverrideProperty(
         GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE), GUICE_BERRY_ENV_THAT_DOES_NOT_EXIST);
    
    try {
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (IllegalArgumentException expected) {}
  }

  public void testTestGetsInjectedWithTestId() {
    
    TestAnnotatedWithStubService1 testClass =  
      TestAnnotatedWithStubService1.createInstance();
    
    assertNull(testClass.testId);
    GuiceBerryJunit3.setUp(testClass);
    assertNotNull(testClass.testId);

    TestId expectedTestId = new TestId(testClass, testClass.testId.random);

    assertEquals(expectedTestId, testClass.testId);
  } 
  
  public void testDifferentTestsGetInjectedWithDifferentTestId() {
    
    TestAnnotatedWithStubService1 firstTest =  
      TestAnnotatedWithStubService1.createInstance();
    
    GuiceBerryJunit3.setUp(firstTest);
    
    assertEquals(
        new TestId(firstTest, 
            firstTest.testId.random),
        firstTest.testId);
    GuiceBerryJunit3.tearDown(firstTest);
   
    TestAnnotatedWithStubService2 secondTest = 
      TestAnnotatedWithStubService2.createInstance();
    GuiceBerryJunit3.setUp(secondTest);
   
    assertEquals(
        new TestId(secondTest, secondTest.testId.random),
        secondTest.testId);
  }
  
  public void testTestIdGetsInjectedIntoRealServiceDefinedByModule() {
    
    TestAnnotatedWithRealService testClass = 
      TestAnnotatedWithRealService.createInstance();
    
    assertEquals(null, testClass.barService);    
    
    GuiceBerryJunit3.setUp(testClass);
    
    assertNotNull(testClass.barService.getTestId());
    assertEquals(
        new TestId(testClass, testClass.barService.getTestId().random),
        testClass.barService.getTestId());
  }
  
  public void testTestGetsInjectedWithTestCase() {  
    TestAnnotatedWithStubService1 testClass =  
      TestAnnotatedWithStubService1.createInstance();
    
    assertEquals(null, testClass.testCase);
    GuiceBerryJunit3.setUp(testClass);
    assertEquals(testClass.getName(), testClass.testCase.getName());
  } 
  
  
  public void testDifferentTestsGetsInjectedWithDifferentTestCases() {
    
    TestAnnotatedWithStubService1 firstTest =  
      TestAnnotatedWithStubService1.createInstance();
    
    GuiceBerryJunit3.setUp(firstTest);
    
    assertEquals(firstTest.getName(), firstTest.testCase.getName());
    
    GuiceBerryJunit3.tearDown(firstTest);
   
    TestAnnotatedWithStubService2 secondTest = 
      TestAnnotatedWithStubService2.createInstance();
    GuiceBerryJunit3.setUp(secondTest);
    
    assertEquals(secondTest.getName(), secondTest.testCase.getName());
    
    JUnitAsserts.assertNotEqual(firstTest.testCase, secondTest.testCase);
  }

  public void testMethodTearDownWorksProperly() {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();
    
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
    
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();
    try {
      GuiceBerryJunit3.tearDown(testClass);
      fail();
    } catch (RuntimeException expected) {}  
  }
  
  
  public void testTearDownNoPreviousSetupOnClassWithAnnotationThatWasUsed() {

    TestAnnotatedWithStubService1 testClass1 = 
      TestAnnotatedWithStubService1.createInstance();
    GuiceBerryJunit3.setUp(testClass1);
    testClass1.run();
    TestAnnotatedWithStubService1 testClass2 = 
      TestAnnotatedWithStubService1.createInstance();
    try {
      GuiceBerryJunit3.tearDown(testClass2);
      fail();
    } catch (RuntimeException expected) {}
  }
  
  
  public void testTearDownOnDifferentClassThatSetupWasCalled() {

    TestAnnotatedWithStubService1 testClass1 = 
      TestAnnotatedWithStubService1.createInstance();
    GuiceBerryJunit3.setUp(testClass1);
    TestAnnotatedWithStubService2 testClass2 = 
      TestAnnotatedWithStubService2.createInstance();
    try {
      GuiceBerryJunit3.tearDown(testClass2);
      fail();
    } catch (RuntimeException expected) {}
  }
  
 
  public void testCallingTwoSetupWithNoTearDownBetween() {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();
    try {
      GuiceBerryJunit3.setUp(testClass);
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (RuntimeException expected) {}   
  }
  
  public void testAddRequiredTearDownToTearDownTestCase() {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();
   
    GuiceBerryJunit3.setUp(testClass);
    assertEquals(testClass, testClass.testCase);
    testClass.run();
  //No concurrence problems as the actual TestCase is: ThreadLocal<TestCase>
    assertNull(GuiceBerryJunit3.getActualTestCase());
  }

  public void testTestCaseCanBeUsedInsteadOfTearDownTestCase() {
    TestCaseAnnotatedWithStubService testClass = 
      TestCaseAnnotatedWithStubService.createInstance();
    GuiceBerryJunit3.setUp(testClass);
    testClass.run();     
  }
  
  public void testMethodTearDownForTestCaseNotCalledAutomatically() {
    TestCaseAnnotatedWithStubService testClass = 
      TestCaseAnnotatedWithStubService.createInstance();
    GuiceBerryJunit3.setUp(testClass);
    testClass.run();

    TestAnnotatedWithStubService1 testClass2 = 
      TestAnnotatedWithStubService1.createInstance();
    try {  
      GuiceBerryJunit3.setUp(testClass2);
      fail();
    } catch (RuntimeException expected) {}
  }
 
  public void testMethodTearDownForTestCaseCalledManually() {
    TestCaseAnnotatedWithStubService testClass = 
      TestCaseAnnotatedWithStubService.createInstance();
    GuiceBerryJunit3.setUp(testClass);   
    testClass.run();
 
    GuiceBerryJunit3.tearDown(testClass);
    TestAnnotatedWithStubService1 testClass2 = 
      TestAnnotatedWithStubService1.createInstance();
    GuiceBerryJunit3.setUp(testClass2);
    testClass2.run();
  }   
 
  public void testAnnotationWithModuleThaHasNoTestScopeListenerBinding() {
    TestAnnotatedWithModuleThatProvidedNoTestScopeListener testClass = 
      TestAnnotatedWithModuleThatProvidedNoTestScopeListener.createInstance();
    try {
      GuiceBerryJunit3.setUp(testClass);
      fail();
    } catch (RuntimeException expected) {}  
  }
  
  public void testModuleThatBindsTestScopeListenerToNoOpTestScopeListener() 
      throws ClassNotFoundException {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();

    GuiceBerryJunit3.setUp(testClass);
    TestScopeListener scopeListener =
      GuiceBerryJunit3.getInjectorFromGB(
      Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE))
      .getInstance(TestScopeListener.class);
    
    assertTrue(scopeListener instanceof NoOpTestScopeListener);    
  }
  
  public void testModuleThatBindsTestScopeListenerToSomeScopeListener() 
      throws ClassNotFoundException {
    TestAnnotatedWithRealService testClass1 = 
      TestAnnotatedWithRealService.createInstance();
      
      GuiceBerryJunit3.setUp(testClass1);
      TestScopeListener scopeListener =
        GuiceBerryJunit3.getInjectorFromGB(
        Class.forName(GuiceBerryEnvTwo.GUICE_BERRY_ENV_TWO))
        .getInstance(TestScopeListener.class);
      
      assertTrue(scopeListener instanceof BazService);    
  }
 
  public void testTearDownOnModuleNoTestScopeListenerBindingNoPreviousSetUp() {

    TestAnnotatedWithModuleThatProvidedNoTestScopeListener testClass = 
      TestAnnotatedWithModuleThatProvidedNoTestScopeListener.createInstance();
    
    try {
      GuiceBerryJunit3.tearDown(testClass);
      fail();
    } catch (RuntimeException expected) {}  
  }
   
  public void testTestScopeListenerGetsNotifiesThatTestEntersTheScope() {
    TestAnnotatedWithRealService testClass = 
      TestAnnotatedWithRealService.createInstance();
   
    long baz = BazService.counter;
    
    assertNotNull(baz);
    GuiceBerryJunit3.setUp(testClass);
    assertNotNull(testClass.baz);
    long baz2 = testClass.baz.getCounter();
     
    assertTrue(baz < baz2);   
    
  }
 
  public void testTestScopeListenerGetsNotifiesThatTestExitsTheScope() {
    TestAnnotatedWithRealService testClass = 
      TestAnnotatedWithRealService.createInstance();
    
    GuiceBerryJunit3.setUp(testClass);
    assertNotNull(testClass.baz);
    long baz = testClass.baz.getCounter();
    GuiceBerryJunit3.tearDown(testClass);
    long baz2 = testClass.baz.getCounter();
    assertTrue(baz < baz2);   
  }

  public void testTestScopeIsCreatedForModule() 
      throws ClassNotFoundException {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();
    
   assertNull(GuiceBerryJunit3.getTestScopeFromGB(
       Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    GuiceBerryJunit3.setUp(testClass);
    JunitTestScope testScope = 
      GuiceBerryJunit3.getTestScopeFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNotNull(testScope);
  }  
 
  public void testReUseTestScopeByTwoTestsAnnotatedWithTheSameModule() 
    throws ClassNotFoundException{
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();
    assertNull(
        GuiceBerryJunit3.getTestScopeFromGB(
            Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    GuiceBerryJunit3.setUp(testClass);
    JunitTestScope testScope = 
      GuiceBerryJunit3.getTestScopeFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNotNull(testScope);
    
    testClass.run();
   
    TestAnnotatedWithStubService2 testClass2 = 
      TestAnnotatedWithStubService2.createInstance();
    GuiceBerryJunit3.setUp(testClass2);
    assertNotNull(GuiceBerryJunit3.getTestScopeFromGB(
        Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    
    JunitTestScope testScope2 = 
      GuiceBerryJunit3.getTestScopeFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertSame(testScope, testScope2);
    assertEquals(1, GuiceBerryJunit3.numberOfInjectorsInUse());
  }  

  public void testUseDifferentTestScopeByTwoTestsAnnotatedWithDifferentModule() 
      throws ClassNotFoundException {
    TestAnnotatedWithStubService1 testClass = 
      TestAnnotatedWithStubService1.createInstance();
   
    assertNull(GuiceBerryJunit3.getTestScopeFromGB(
        Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)));
    GuiceBerryJunit3.setUp(testClass);
    JunitTestScope testScope = 
      GuiceBerryJunit3.getTestScopeFromGB(
          Class.forName(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE));
    assertNotNull(testScope);
  
    testClass.run();
    
    TestAnnotatedWithRealService testClass2 = 
      TestAnnotatedWithRealService.createInstance();
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
    TestAnnotatedWithModuleInjectsTestCaseInTestScopeListener testClass = 
      TestAnnotatedWithModuleInjectsTestCaseInTestScopeListener.createInstance();
    
    GuiceBerryJunit3.setUp(testClass);
    testClass.run();
  }
  
//THE BELOW CLASSES ARE USED ONLY FOR TESTING GuiceBerry
  
  public static final class MyGuiceBerryEnvRemapper implements GuiceBerryEnvRemapper {

    public String remap(TestCase test, String guiceBerryEnv) {
      return GuiceBerryEnvTwo.class.getName();
    }
  }

  private static final class MyInvalidGuiceBerryEnvRemapper implements GuiceBerryEnvRemapper {
    public MyInvalidGuiceBerryEnvRemapper(int foo) {
    }

    public String remap(TestCase test, String guiceBerryEnv) {
      return null;
    }
  }

  private static final class MyNonGuiceBerryEnvRemapper {    
  }
  
  @GuiceBerryEnv(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)
  private static final class TestAnnotatedWithStubService1 
      extends TearDownTestCase {
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
  
    static TestAnnotatedWithStubService1 createInstance() {
      TestAnnotatedWithStubService1 result = 
        new TestAnnotatedWithStubService1();
      result.setName("fooTest");
      return result;
    }    
  }
  
  @GuiceBerryEnv(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)
  private static final class TestAnnotatedWithStubService2 
      extends TearDownTestCase {
    
    @Inject
    private int number;
    
    @Inject
    private TestId testId;
    
    @Inject 
    private TestCase testCase;

    static TestAnnotatedWithStubService2 createInstance() {
      TestAnnotatedWithStubService2 result = 
        new TestAnnotatedWithStubService2();
      result.setName(TestAnnotatedWithStubService2.class.getCanonicalName()); 
      return result;
    }
  }
  
  @GuiceBerryEnv(GuiceBerryEnvOne.GUICE_BERRY_ENV_ONE)
  private static final class TestCaseAnnotatedWithStubService extends TestCase {
    @Inject
    TestCase testCase;
    
    static TestCaseAnnotatedWithStubService createInstance() {
      TestCaseAnnotatedWithStubService result = 
        new TestCaseAnnotatedWithStubService();
      result.setName(TestCaseAnnotatedWithStubService
          .class.getCanonicalName());
      return result;
    }  
  }
  
  @GuiceBerryEnv(GuiceBerryEnvTwo.GUICE_BERRY_ENV_TWO)
  private static final class TestAnnotatedWithRealService 
      extends TearDownTestCase {
    @Inject
    private FooService fooService;

    @Inject
    private BarService barService;  
    
    @Inject
    private int number;
 
    @Inject
    private BazService baz;
   
    static TestAnnotatedWithRealService createInstance() {
      TestAnnotatedWithRealService result = new TestAnnotatedWithRealService();
      result.setName(TestAnnotatedWithRealService.class.getCanonicalName());
      return result;
    }  
  }
  
  @GuiceBerryEnv(GUICE_BERRY_ENV_THAT_DOES_NOT_EXIST)
  private static final class TestAnnotatedWithModuleThatNotExist 
      extends TearDownTestCase {
    static TestAnnotatedWithModuleThatNotExist createInstance() {
      TestAnnotatedWithModuleThatNotExist result = 
        new TestAnnotatedWithModuleThatNotExist();
      result.setName(TestAnnotatedWithModuleThatNotExist
          .class.getCanonicalName());
      return result;
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvWithoutBindingsForFooOrBar.GUICE_BERRY_ENV_WITHOUT_BINDINGS_FOR_FOO_OR_BAR)
  private static final class TestAnnotatedWithModuleThatHasMissingBindings 
      extends TearDownTestCase {
    @Inject
    BarService barService; 
    
    static TestAnnotatedWithModuleThatHasMissingBindings createInstance() {
      TestAnnotatedWithModuleThatHasMissingBindings result = 
        new TestAnnotatedWithModuleThatHasMissingBindings();
      result.setName(TestAnnotatedWithModuleThatHasMissingBindings
          .class.getCanonicalName());
      return result;
    }  
  }
  
  @GuiceBerryEnv(NotAGuiceBerryEnvOne.NOT_A_GUICE_BERRY_ENV_ONE)
  private static final class TestAnnotatedWithClassThatNotImplementsModule 
      extends TearDownTestCase {
    
    static TestAnnotatedWithClassThatNotImplementsModule createInstance() {
      TestAnnotatedWithClassThatNotImplementsModule result = 
        new TestAnnotatedWithClassThatNotImplementsModule();
      result.setName(TestAnnotatedWithClassThatNotImplementsModule
          .class.getCanonicalName());
      return result;
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvWithIllegalConstructor.GUICE_BERRY_ENV_WITH_ILLEGAL_CONSTRUCTOR)
  private static final class TestAnnotatedWithModuleThatHAsWrongConstructor
    extends TearDownTestCase {
    
    static TestAnnotatedWithModuleThatHAsWrongConstructor createInstance() {
      TestAnnotatedWithModuleThatHAsWrongConstructor result = 
        new TestAnnotatedWithModuleThatHAsWrongConstructor();
      result.setName(TestAnnotatedWithModuleThatHAsWrongConstructor
          .class.getCanonicalName());
      return result;
    }  
  }

  @GuiceBerryEnv(GuiceBerryEnvWithNoTestScopeListener.GUICE_BERRY_ENV_WITH_NO_TEST_SCOPE_LISTENER)
  private static final class TestAnnotatedWithModuleThatProvidedNoTestScopeListener 
      extends TearDownTestCase {
    
    static TestAnnotatedWithModuleThatProvidedNoTestScopeListener createInstance() {
      TestAnnotatedWithModuleThatProvidedNoTestScopeListener result = 
        new TestAnnotatedWithModuleThatProvidedNoTestScopeListener();
      result.setName(TestAnnotatedWithModuleThatProvidedNoTestScopeListener
          .class.getCanonicalName());
      return result;
    }  
  }

  @GuiceBerryEnv(NOT_A_GUICE_BERRY_ENV_BECAUSE_IT_IS_ABSTRACT)
  private static final class TestAnnotatedWithModuleThatIsAbstractClass 
      extends TearDownTestCase {
    
    static TestAnnotatedWithModuleThatIsAbstractClass createInstance() {
      TestAnnotatedWithModuleThatIsAbstractClass result = 
        new TestAnnotatedWithModuleThatIsAbstractClass();
      result.setName(TestAnnotatedWithModuleThatIsAbstractClass
          .class.getCanonicalName());
      return result;
    }  
  }
  
  @GuiceBerryEnv(GuiceBerryEnvWithNonTrivialTestScopeListener.MODULE_NAME_INJECTS_TEST_CASE_IN_TEST_SCOPE_LISTENER)
  private static final class TestAnnotatedWithModuleInjectsTestCaseInTestScopeListener
      extends TestCase {
    
    static TestAnnotatedWithModuleInjectsTestCaseInTestScopeListener createInstance() {
      TestAnnotatedWithModuleInjectsTestCaseInTestScopeListener result = 
        new TestAnnotatedWithModuleInjectsTestCaseInTestScopeListener();
      result.setName(TestAnnotatedWithModuleInjectsTestCaseInTestScopeListener.class
          .getCanonicalName());
      return result;
    }  
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

  public static class GuiceBerryEnvTwo extends AbstractModule {
    private static final String GUICE_BERRY_ENV_TWO = 
    GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$GuiceBerryEnvTwo";

    @Override
    public void configure() {
      install(new BasicJunit3Module());
      bind(FooService.class).to(FooServiceTwo.class);
      bind(BarService.class).to(BarServiceTwo.class);      
      bind(BazService.class).in(Singleton.class);
      bind(Integer.class).toInstance(NUMBER++);
      bind(String.class).toInstance(INJECTED_INFORMATION);
      bind(TestScopeListener.class).to(BazService.class).in(Scopes.SINGLETON);
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
      bind(String.class).toInstance(INJECTED_INFORMATION);
    }
  }   
  
  /**
   * {@link GuiceBerryEnv}s must be {@link Module}s.
   */
  public static class NotAGuiceBerryEnvOne {

    private static final String NOT_A_GUICE_BERRY_ENV_ONE = 
    GuiceBerryJunit3Test.SELF_CANONICAL_NAME + "$NotAGuiceBerryEnvOne"; }

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
    
    public void enteringScope() {
      Objects.nonNull(testCase, "TestCase is null, ");
    }

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
