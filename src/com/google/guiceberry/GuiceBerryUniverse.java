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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.common.testing.TearDownStack;
import com.google.guiceberry.GuiceBerry.GuiceBerryWrapper;
import com.google.guiceberry.GuiceBerryModule.ToTearDown;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3;

import java.util.Map;

/**
 * @author Luiz-Otavio "Z" Zorzella
 */
class GuiceBerryUniverse {

  static final GuiceBerryUniverse INSTANCE = new GuiceBerryUniverse();
  
  final Map<Class<? extends Module>, Injector> gbeClassToInjectorMap = Maps.newHashMap();
  
  public final InheritableThreadLocal<TestDescription> currentTestDescriptionThreadLocal =
    new InheritableThreadLocal<TestDescription>();
  
  /**
   * If something goes wrong trying to get an Injector instance for some 
   * GuiceBerryEnv, this instance is stored in the 
   * {@link GuiceBerryUniverse#gbeClassToInjectorMap}, to allow for graceful
   * error handling.
   */
  private static final Injector BOGUS_INJECTOR = Guice.createInjector(new GuiceBerryModule());
  
  static class TestCaseScaffolding implements GuiceBerryWrapper {

    private final TestDescription testDescription;
    private final GuiceBerryEnvSelector guiceBerryEnvSelector;
    private final GuiceBerryUniverse universe;

    private Injector injector;
    
    private final TearDownStack stack = new TearDownStack();
    
    public TestCaseScaffolding(
        TestDescription testDescription,
        GuiceBerryEnvSelector guiceBerryEnvSelector,
        GuiceBerryUniverse universe) {
      this.testDescription = Preconditions.checkNotNull(testDescription);
      this.guiceBerryEnvSelector = Preconditions.checkNotNull(guiceBerryEnvSelector);
      this.universe = Preconditions.checkNotNull(universe);
    }

    public synchronized void runBeforeTest() {
      
      // If anything should go wrong, we "tag" this scaffolding as having failed
      // to acquire an injector, so that the tear down knows to skip the
      // appropriate steps.
      injector = BOGUS_INJECTOR;

      checkPreviousTestCalledTearDown(testDescription);
      
      final Class<? extends Module> gbeClass =
        guiceBerryEnvSelector.guiceBerryEnvToUse(testDescription);
      
      universe.currentTestDescriptionThreadLocal.set(testDescription);
      injector = getAndSetInjector(gbeClass);

      stack.addTearDown(new TearDown() {
        public void tearDown() throws Exception {
          doTearDown();
        }
      });
      
      stack.addTearDown(new TearDown() {
        public void tearDown() throws Exception {
          ToTearDown toTearDown = injector.getInstance(ToTearDown.class);
          toTearDown.runTearDown();
        }
      });
      TearDownAccepter accepter = wrappedGetInstance(injector, TearDownAccepter.class, gbeClass);
      buildTestWrapperInstance(injector).toRunBeforeTest();
      
      injectMembersIntoTest(gbeClass, injector); 
    }

    private void injectMembersIntoTest(
        final Class<? extends Module> gbeClass, Injector injector) {
    
      try {
        injector.injectMembers(testDescription.getTestCase());
      } catch (ConfigurationException e) {
        String msg = String.format("Binding error in the GuiceBerry Env '%s': '%s'.",
            gbeClass.getName(), e.getMessage());
        throw new RuntimeException(msg, e);
      }
    }

    private static <T> T wrappedGetInstance(
        final Injector injector, 
        final Class<T> clazz,
        final Class<? extends Module> gbeClass
        ) {
      
      try {
        return injector.getInstance(clazz);
      } catch (ConfigurationException e) {
        String msg = String.format("Binding error in the GuiceBerry Env '%s': '%s'.",
            gbeClass.getName(), e.getMessage());
        throw new RuntimeException(msg, e);
      }
    }
    
    /**
     * Returns the {@link Injector} for the given {@code gbeClass}. If this
     * GuiceBerry env has never been seen before, add it to the 
     * {@link #gbeClassToInjectorMap}.
     */
    private Injector getAndSetInjector(final Class<? extends Module> gbeClass) {
      synchronized (universe.gbeClassToInjectorMap) {
        if (!universe.gbeClassToInjectorMap.containsKey(gbeClass)) {
          foundGbeForTheFirstTime(gbeClass);  
        }
      }
      
      Injector result = universe.gbeClassToInjectorMap.get(gbeClass);
      if (result == BOGUS_INJECTOR) {
        throw new RuntimeException(String.format(
            "Skipping '%s' GuiceBerryEnv which failed previously during injector creation.",
            gbeClass.getName()));
      }
      return result; 
    }

    private void checkPreviousTestCalledTearDown(TestDescription testCase) {
      TestDescription previousTestCase = universe.currentTestDescriptionThreadLocal.get();
      
      if (previousTestCase != null) {  
        String msg = String.format(
            "Error while setting up a test: GuiceBerry was asked to " +
            "set up test '%s', but the previous test '%s' did not properly " +
            "call GuiceBerry's tear down.",
            testCase.getName(),
            previousTestCase.getName());
        throw new RuntimeException(msg);
      }
    }
    
    private void foundGbeForTheFirstTime(final Class<? extends Module> gbeClass) {
      Injector result = BOGUS_INJECTOR;
      try {
        Module gbeInstance = createGbeInstanceFromClass(gbeClass);
        Injector injector = Guice.createInjector(gbeInstance);
        callGbeMainIfBound(injector);
        // We don't actually use the test wrapper here, but we make sure we can
        // get an instance (i.e. we fail fast).
        buildTestWrapperInstance(injector);
        result = injector;
      } finally {
        // This is in the finally block to ensure that BOGUS_INJECTOR
        // is put in the map if things go bad.
        universe.gbeClassToInjectorMap.put(gbeClass, result);
      }
    }

    private static TestWrapper buildTestWrapperInstance(Injector injector) {
      TestWrapper result = NoOpTestScopeListener.NO_OP_INSTANCE;
      try {
        boolean hasTestScopeListenerBinding = hasTestScopeListenerBinding(injector);
        boolean hasDeprecatedTestScopeListenerBinding = hasDeprecatedTestScopeListenerBinding(injector);
        if (hasTestScopeListenerBinding && hasDeprecatedTestScopeListenerBinding) {
          throw new RuntimeException(
            "Your GuiceBerry Env has bindings for both the new TestScopeListener and the deprecated one. Please fix.");
        } else if (hasTestScopeListenerBinding) {
          result = injector.getInstance(TestWrapper.class);
        } else if (hasDeprecatedTestScopeListenerBinding) {
          result = adapt(
              injector.getInstance(com.google.inject.testing.guiceberry.TestScopeListener.class),
              injector.getInstance(TearDownAccepter.class));
        }
      } catch (ConfigurationException e) {
        String msg = String.format("Error while creating a TestWrapper: '%s'.",
          e.getMessage());
        throw new RuntimeException(msg, e); 
      }
      return result;
    }

    private static TestWrapper adapt(
        final com.google.inject.testing.guiceberry.TestScopeListener instance,
        final TearDownAccepter tearDownAccepter) {
      return new TestWrapper() {

        public void toRunBeforeTest() {
          tearDownAccepter.addTearDown(new TearDown() {
            public void tearDown() throws Exception {
              instance.exitingScope();
            }
          });
          instance.enteringScope();
        }
      };
    }

    private static boolean hasBinding(Injector injector, Class<?> clazz) {
      return injector.getBindings().get(Key.get(clazz)) != null;
    }

    private static <T> T getInstanceIfHasBinding(Injector injector, Class<T> clazz) {
      if (hasBinding(injector, clazz)) {
        return injector.getInstance(clazz);
      }
      return null;
    }
    
    
    private static boolean hasDeprecatedTestScopeListenerBinding(Injector injector) {
      return hasBinding(injector, com.google.inject.testing.guiceberry.TestScopeListener.class);
    }

    private static boolean hasTestScopeListenerBinding(Injector injector) {
      return hasBinding(injector, TestWrapper.class);
    }

    private static void callGbeMainIfBound(Injector injector) {
      com.google.inject.testing.guiceberry.GuiceBerryEnvMain deprecatedGuiceBerryEnvMain = 
        getInstanceIfHasBinding(injector, com.google.inject.testing.guiceberry.GuiceBerryEnvMain.class);

      GuiceBerryEnvMain guiceBerryEnvMain = 
        getInstanceIfHasBinding(injector, GuiceBerryEnvMain.class);
      
      if ((deprecatedGuiceBerryEnvMain != null) && (guiceBerryEnvMain != null)) {
        throw new RuntimeException(String.format(
            "You have bound both the deprecated and the new versions of GuiceBerryEnvMain ('%s' and '%s'). "
            + "Please remove the binding to the deprecated one.", 
            deprecatedGuiceBerryEnvMain.getClass().getName(),
            guiceBerryEnvMain.getClass().getName()));
      }
      
      if (deprecatedGuiceBerryEnvMain != null) {
        deprecatedGuiceBerryEnvMain.run();
      }
      
      if (guiceBerryEnvMain != null) {
        guiceBerryEnvMain.run();
      }
    }

    private static Module createGbeInstanceFromClass(final Class<? extends Module> gbeClass) {
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
    
    public void runAfterTest() {
      if (injector == BOGUS_INJECTOR) {
        // We failed to get a valid injector for this module in the setUp method,
        // so we just gracefully return, after cleaning up the threadlocal (which
        // normally would happen in the doTearDown method).
        universe.currentTestDescriptionThreadLocal.remove();
        return;
      }
      stack.runTearDown();
    }
    
    private void doTearDown() {
      if (!universe.currentTestDescriptionThreadLocal.get().equals(testDescription)) {
        String msg = String.format(GuiceBerryJunit3.class.toString() 
            + " cannot tear down "
            + testDescription.toString()
            + " because that test never called "
            + GuiceBerryJunit3.class.getCanonicalName()
            + ".setUp()"); 
        throw new RuntimeException(msg); 
      }
      universe.currentTestDescriptionThreadLocal.remove();
      injector.getInstance(TestScope.class).finishScope(testDescription);    
    }
  }

  private static final class NoOpTestScopeListener implements TestWrapper {
    
    private static final TestWrapper NO_OP_INSTANCE = new NoOpTestScopeListener();

    public void toRunBeforeTest() {}
  }
}