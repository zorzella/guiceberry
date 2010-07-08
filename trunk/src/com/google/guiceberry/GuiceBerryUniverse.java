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
package com.google.guiceberry;

import com.google.common.collect.Maps;
import com.google.guiceberry.GuiceBerryModule.ToTearDown;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.GuiceBerryEnvMain;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3;

import java.util.Map;

/**
 * Do <em>not</em> use this class, or any of its methods directly -- it is an
 * internal support class, and its methods are subject to change without notice!
 * 
 * <p>Instead, use one of these:
 * 
 * <ul>
 *   <li>For Junit4, {@link }
 *   <li>For Junit3 with TearDownTestCase, {@link }
 *   <li>For plain Junit3 {@link }
 * </ul>
 *
 * <p>
 * 
 * @author Luiz-Otavio Zorzella
 */
public class GuiceBerryUniverse {

  public static final GuiceBerryUniverse INSTANCE = new GuiceBerryUniverse();
  
  final Map<Class<? extends Module>, Injector> gbeClassToInjectorMap = Maps.newHashMap();
  
  public final InheritableThreadLocal<TestDescription> currentTestDescriptionThreadLocal =
    new InheritableThreadLocal<TestDescription>();
  
  /**
   * If something goes wrong trying to get an Injector instance for some 
   * GuiceBerryEnv, this instance is stored in the 
   * {@link GuiceBerryUniverse#gbeClassToInjectorMap}, to allow for graceful
   * error handling.
   */
  private static final Injector BOGUS_INJECTOR = Guice.createInjector();
  
  public class TestCaseScaffolding {

    private final TestDescription testDescription;
    private final EnvChooser envChooser;
    private final GuiceBerryUniverse universe;

    private Injector injector;
    
    public TestCaseScaffolding(
        TestDescription testDescription,
        EnvChooser envChooser) {
      this.testDescription = testDescription;
      this.envChooser = envChooser;
      this.universe = GuiceBerryUniverse.this;
    }

    public synchronized void goSetUp() {
      
      // If anything should go wrong, we "tag" this scaffolding as having failed
      // to acquire an injector, so that the tear down knows to skip the
      // appropriate steps.
      injector = BOGUS_INJECTOR;

      checkPreviousTestCalledTearDown(testDescription);
      
      final Class<? extends Module> gbeClass =
        envChooser.guiceBerryEnvToUse(testDescription);
      
      universe.currentTestDescriptionThreadLocal.set(testDescription);
      injector = getInjector(gbeClass);

      injector.getInstance(TestScopeListener.class).enteringScope();
      injectMembersIntoTest(gbeClass, injector); 
    }

    private void injectMembersIntoTest(
        final Class<? extends Module> gbeClass, Injector injector) {
    
      try {
        injector.injectMembers(testDescription.testCase);
      } catch (ConfigurationException e) {
        String msg = String.format("Binding error in the GuiceBerry Env '%s': '%s'.",
            gbeClass.getName(), e.getMessage());
        notifyTestScopeListenerOfOutScope(universe.gbeClassToInjectorMap.get(gbeClass));
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

    private void checkPreviousTestCalledTearDown(TestDescription testCase) {
      TestDescription previousTestCase = universe.currentTestDescriptionThreadLocal.get();
      
      if (previousTestCase != null) {  
        String msg = String.format(
            "Error while setting up a test: GuiceBerry was asked to " +
            "set up test '%s', but the previous test '%s' did not properly " +
            "call GuiceBerry's tear down.",
            testCase.name,
            previousTestCase.name);
        throw new RuntimeException(msg);
      }
    }
    
    private Injector foundGbeForTheFirstTime(final Class<? extends Module> gbeClass) {
      
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
          String msg = String.format("Error while creating a TestScopeListener: '%s'.",
            e.getMessage());
          throw new RuntimeException(msg, e); 
        }
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

    private Module createGbeInstanceFromClass(final Class<? extends Module> gbeClass) {
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
    
    public void goTearDown() {
      
      if (injector == BOGUS_INJECTOR) {
        // We failed to get a valid injector for this module in the setUp method,
        // so we just gracefully return, after cleaning up the threadlocal (which
        // normally would happen in the doTearDown method).
        universe.currentTestDescriptionThreadLocal.set(null);
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
    
    private void notifyTestScopeListenerOfOutScope(Injector injector) {
      injector.getInstance(TestScopeListener.class).exitingScope();
    }

    private void doTearDown(Injector injector) {
    
      if (!universe.currentTestDescriptionThreadLocal.get().equals(testDescription)) {
        String msg = String.format( GuiceBerryJunit3.class.toString() 
            + " cannot tear down "
            + testDescription.toString()
            + " because that test never called "
            + GuiceBerryJunit3.class.getCanonicalName()
            + ".setUp()"); 
        throw new RuntimeException(msg); 
      }
      universe.currentTestDescriptionThreadLocal.set(null);
      injector.getInstance(TestScope.class).finishScope(testDescription);    
    }
  }

  
}