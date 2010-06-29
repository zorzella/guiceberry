/*
 * Copyright (C) 2010 Google Inc.
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
import com.google.common.testing.TearDownAccepter;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.GuiceBerryEnvMain;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.BasicJunit3Module.ToTearDown;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3.GuiceBerryUniverse;

import junit.framework.TestCase;

class TestCaseScafolding {

  /**
   * If something goes wrong trying to get a valid instance of an Injector
   * for some GuiceBerryEnv name, this instance is stored in the 
   * {@link #gbeClassToInjectorMap}, to allow for graceful error handling.
   */
  private static final Injector BOGUS_INJECTOR = Guice.createInjector();
  
  private final TestCase testCase;
  private final GuiceBerryEnv gbeAnnotation;
  private final GuiceBerryEnvRemappersCoupler remapper;

  private final GuiceBerryUniverse universe;

  public TestCaseScafolding(
      TestCase testCase, 
      GuiceBerryEnv gbeAnnotation,
      GuiceBerryEnvRemappersCoupler remapper, 
      GuiceBerryUniverse universe) {
        this.testCase = testCase;
        this.gbeAnnotation = gbeAnnotation;
        this.remapper = remapper;
        this.universe = universe;
  }

  synchronized void goSetUp(final TestCase testCase) {
    if (gbeAnnotation == null) {
      throw new IllegalArgumentException(String.format(
              "Test class '%s' must have an @%s annotation.",
              testCase.getClass().getName(), GuiceBerryEnv.class.getSimpleName()));   
    }
    addGuiceBerryTearDown(testCase);
    checkPreviousTestCalledTearDown(testCase);
    //Setup after registering tearDown so that if an exception is thrown here,
    //we still do a tearDown.
    doSetUp();
  }
  
  private void doSetUp() {
    final Class<? extends Module> gbeClass = 
      getGbeClassForTest();
    universe.testCurrentlyRunningOnThisThread.set(testCase);
    Injector injector = getInjector(gbeClass);
    injector.getInstance(TestScopeListener.class).enteringScope();
    injectMembersIntoTest(gbeClass, injector); 
  }

  private void injectMembersIntoTest(
      final Class<? extends Module> moduleClass, Injector injector) {
  
    try {
      injector.injectMembers(testCase);
    } catch (RuntimeException e) {  
      String msg = String.format("Binding error in the module '%s': '%s'.", 
          moduleClass.toString(), e.getMessage());
      notifyTestScopeListenerOfOutScope(universe.gbeClassToInjectorMap.get(moduleClass));
      throw new RuntimeException(msg, e);
    }
  }

  private Injector getInjector(final Class<? extends Module> gbeClass) {
    if (!universe.gbeClassToInjectorMap.containsKey(gbeClass)) {    
      return foundGbeForTheFirstTime(gbeClass);  
    } else {
      Injector injector = 
        universe.gbeClassToInjectorMap.get(gbeClass);
      if (injector == BOGUS_INJECTOR) {
        throw new RuntimeException(String.format(
            "Skipping '%s' GuiceBerryEnv which failed previously during injector creation.",
            gbeClass.getName()));
      }
      return injector; 
    }
  }

  private void checkPreviousTestCalledTearDown(TestCase testCase) {
    TestCase previousTestCase = universe.testCurrentlyRunningOnThisThread.get();
    
    if (previousTestCase != null) {  
      String msg = String.format("Error while setting up a test: %s asked to " +
            "set up test: %s.%s, but previous test:%s.%s did not properly " +
            "call %s.tearDown().",
          GuiceBerryJunit3.class.getCanonicalName(),
          testCase.getClass().getCanonicalName(),
          testCase.getName(), 
          previousTestCase.getClass().getCanonicalName(),
          previousTestCase.getName(),
          GuiceBerryJunit3.class.getCanonicalName());
      throw new RuntimeException(msg);
    }
  }
  
  private Injector foundGbeForTheFirstTime(
      final Class<? extends Module> gbeClass) {
    
    Injector injector = BOGUS_INJECTOR;
    
    try {
      Module gbeInstance = createGbeInstanceFromClass(gbeClass);
      injector = Guice.createInjector(gbeInstance);
      callGbeMainIfBound(injector);
      try {
        if (injector.getBindings().get(Key.get(TestScopeListener.class)) == null) {
          String msg = "TestScopeListener must be bound in your GuiceBerryEnv.";
          throw new RuntimeException(msg); 
        }
        // This is not actually used, but ensures at this point that the bound
        // TestScopeListener can be created
        @SuppressWarnings("unused")
        TestScopeListener testScopeListener = 
          injector.getInstance(TestScopeListener.class);
      } catch (ConfigurationException e) {
        String msg = String.format("Error while creating the instance of: " +
            "'%s': '%s'.", TestScopeListener.class, e.getMessage());
        throw new RuntimeException(msg, e); 
      }

      JunitTestScope testScope = injector.getInstance(JunitTestScope.class);
      return injector;
    } finally {
      // This is in the finally block to ensure that BOGUS_INJECTOR
      // is put in the map if things go bad.
      universe.gbeClassToInjectorMap.put(gbeClass, injector);
    }
  }

  private void callGbeMainIfBound(Injector injector) {
    if (injector.getBindings().get(Key.get(GuiceBerryEnvMain.class)) != null) {
      injector.getInstance(GuiceBerryEnvMain.class).run();
    }
  }

  private Module createGbeInstanceFromClass(
      final Class<? extends Module> gbeClass) {
    Module result; 
    try {
      result = gbeClass.getConstructor().newInstance(); 
    } catch (NoSuchMethodException e) {
      String msg = String.format(
              "@%s class '%s' must have a public zero-arguments constructor",
              GuiceBerryEnv.class.getSimpleName(),
              gbeClass.getName()); 
      throw new IllegalArgumentException(msg, e); 
    } catch (Exception e) {
          String msg = String.format(
                  "Error creating instance of @%s '%s'",
                  GuiceBerryEnv.class.getSimpleName(),
                  gbeClass.getName()); 
            throw new IllegalArgumentException(msg, e); 
    }
    return result;
  }
  
  private void addGuiceBerryTearDown(final TestCase testCase) {
    if (testCase instanceof TearDownAccepter) {
      TearDownAccepter tdtc = (TearDownAccepter) testCase;
      tdtc.addTearDown(new TearDown() {
        public void tearDown() {
          goTearDown();
        }
      });
    }
  }  

  void goTearDown() {
    Class<? extends Module> gbeClass = getGbeClassForTest();
    
    Injector injector = 
      universe.gbeClassToInjectorMap.get(gbeClass);
    if (injector == BOGUS_INJECTOR) {
      // We failed to get a valid injector for this module in the setUp method,
      // so we just gracefully return, after cleaning up the threadlocal (which
      // normally would happen in the doTearDown method).
      universe.testCurrentlyRunningOnThisThread.set(null);
      return;
    }
    
    ToTearDown toTearDown = injector.getInstance(ToTearDown.class);
    toTearDown.runTearDown();
    
    notifyTestScopeListenerOfOutScope(injector);
    // TODO: this line used to be before the notifyTestScopeListenerOfOutScope
    // causing a bug -- e.d. a Provider<TestId> could not be used in the 
    // exitingScope method of the TestScopeListener. I haven't yet found a
    // good way to test this change.
    doTearDown(injector);
  }
  
  @SuppressWarnings("unchecked") 
  private Class<? extends Module> getGbeClassForTest() {
  
    String gbeName = getGbeName(testCase);
    Class<? extends Module> gbeClass = 
      (Class<? extends Module>) getGbeClassFromClassName(gbeName);
    if (!Module.class.isAssignableFrom(gbeClass)) {
      String msg = String.format(
          "@%s class '%s' must be a Guice Module (i.e. implement com.google.inject.Module).", 
          GuiceBerryEnv.class.getSimpleName(),
          gbeClass.getName()); 
      throw new IllegalArgumentException(msg);
    }
    return gbeClass;
  }
  
  private static Class<?> getGbeClassFromClassName(
      String gbeName) {
    Class<?> className;
    try {
      className = GuiceBerryJunit3.class.getClassLoader().loadClass(gbeName);   
    } catch (ClassNotFoundException e) {  
      String msg = String.format(
              "@%s class '%s' was not found.",
              GuiceBerryEnv.class.getSimpleName(),
              gbeName.toString());
      throw new IllegalArgumentException(msg, e);
    }
    return className;
  }

  private String getGbeName(TestCase testCase) {
    String declaredGbeName = this.gbeAnnotation.value();
    String result = remapper.remap(testCase, declaredGbeName);
    if (result == null) {
      throw new IllegalArgumentException(String.format(
          "The installed GuiceBerryEnvRemapper '%s' returned 'null' for the " +
          "'%s' test, which declares '%s' as its GuiceBerryEnv", 
          remapper.backing().getClass().getName(), 
          testCase.getName(), declaredGbeName));
    }
    return result;
  }

  private static void notifyTestScopeListenerOfOutScope(Injector injector) {
    injector.getInstance(TestScopeListener.class).exitingScope();
  }

  private void doTearDown(Injector injector) {
  
    if (universe.testCurrentlyRunningOnThisThread.get() != testCase) {
      String msg = String.format( GuiceBerryJunit3.class.toString() 
          + " cannot tear down "
          + testCase.toString()
          + " because that test never called "
          + GuiceBerryJunit3.class.getCanonicalName()
          + ".setUp()"); 
      throw new RuntimeException(msg); 
    }
    universe.testCurrentlyRunningOnThisThread.set(null);
    injector.getInstance(JunitTestScope.class).finishScope(testCase);    
  }
}

