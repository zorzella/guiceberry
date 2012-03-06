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

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Luiz-Otavio "Z" Zorzella
 */
public class GuiceBerryUniverseTest {

  private static TestDescription bogusTestDescription () {
    return new TestDescription(new MyTest(), "bogus test case");
  }

  private static final class MyTest {
    @Inject
    TearDownAccepter accepter;
  }
  
  private static GuiceBerryUniverse universe = null;

  @Before public void setUniverse () {
    if (universe != null) {
      throw new RuntimeException();
    }
    universe = new GuiceBerryUniverse();
  }

  @After public void resetUniverse() {
    universe = null;
  }
  
  public static final class NonGuiceBerryEnvSinceItDoesNotInstallGuiceBerryModule extends AbstractModule {
    @Override
    protected void configure() {}
  }

  @Test public void testExceptionWhenGbeDoesNotInstallGuiceBerryModule0() {
    GuiceBerryEnvSelector guiceBerryEnvSelector =
      DefaultEnvSelector.of(NonGuiceBerryEnvSinceItDoesNotInstallGuiceBerryModule.class);
    TestDescription testDescription = bogusTestDescription();
    GuiceBerryUniverse.TestCaseScaffolding testCaseScaffolding = 
      new GuiceBerryUniverse.TestCaseScaffolding(testDescription, guiceBerryEnvSelector, universe);

    try {
      testCaseScaffolding.runBeforeTest();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(String.format(
          "The GuiceBerry Env '%s' must call 'install(new GuiceBerryModule())' "
          + "in its 'configure()' method, so as to install the bindings defined there.",
          NonGuiceBerryEnvSinceItDoesNotInstallGuiceBerryModule.class.getName()),
          e.getMessage());
    }
    
    testCaseScaffolding.runAfterTest();
  }
  
  public static final class NonGuiceBerryEnvNonAbstractModuleSinceItDoesNotInstallGuiceBerryModule implements Module {
    public void configure(Binder binder) {}
  }

  @Test public void testExceptionWhenGbeDoesNotInstallGuiceBerryModule1() {
    GuiceBerryEnvSelector guiceBerryEnvSelector =
      DefaultEnvSelector.of(NonGuiceBerryEnvNonAbstractModuleSinceItDoesNotInstallGuiceBerryModule.class);
    TestDescription testDescription = bogusTestDescription();
    GuiceBerryUniverse.TestCaseScaffolding testCaseScaffolding = 
      new GuiceBerryUniverse.TestCaseScaffolding(testDescription, guiceBerryEnvSelector, universe);

    try {
      testCaseScaffolding.runBeforeTest();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(String.format(
          "The GuiceBerry Env '%s' must call 'binder.install(new GuiceBerryModule()' "
          + "in its 'configure(Binder)' method, so as to install the bindings defined there.",
          NonGuiceBerryEnvNonAbstractModuleSinceItDoesNotInstallGuiceBerryModule.class.getName()),
          e.getMessage());
    }
    
    testCaseScaffolding.runAfterTest();
  }
  
  public static final class NonGuiceBerryEnvSinceItDoesNotInstallGuiceBerryModuleByNoCallingSuper extends AbstractModule {
    @Override
    protected void configure() {
      install(new GuiceBerryModule());
    }
  }
    
  @Test public void testExceptionWhenGbeDoesNotInstallGuiceBerryModule2() {
    GuiceBerryEnvSelector guiceBerryEnvSelector =
      DefaultEnvSelector.of(NonGuiceBerryEnvSinceItDoesNotInstallGuiceBerryModuleByNoCallingSuper.class);
    TestDescription testDescription = bogusTestDescription();
    GuiceBerryUniverse.TestCaseScaffolding testCaseScaffolding = 
      new GuiceBerryUniverse.TestCaseScaffolding(testDescription, guiceBerryEnvSelector, universe);

    try {
      testCaseScaffolding.runBeforeTest();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals(String.format(
          "The GuiceBerry Env '%s' must call 'super.configure()' "
          + "in its 'configure()' method, so as to install the bindings defined in GuiceBerryModule.",
          NonGuiceBerryEnvSinceItDoesNotInstallGuiceBerryModuleByNoCallingSuper.class.getName()),
          e.getMessage());
    }
    
    testCaseScaffolding.runAfterTest();
  }
  
  private static final class MyGuiceBerryEnv extends AbstractModule {
    
    private final GuiceBerryModule gbm;
    
    @Override
    protected void configure() {
      install(gbm);
    }
    
    @SuppressWarnings("unused")
    public MyGuiceBerryEnv() {
      this.gbm = new GuiceBerryModule(GuiceBerryUniverseTest.universe);
    }
  }
  
  @Test public void testThrowingTearDown() {
    GuiceBerryEnvSelector guiceBerryEnvSelector = DefaultEnvSelector.of(MyGuiceBerryEnv.class);
    TestDescription testDescription = bogusTestDescription();
    GuiceBerryUniverse.TestCaseScaffolding testCaseScaffolding = 
      new GuiceBerryUniverse.TestCaseScaffolding(testDescription, guiceBerryEnvSelector, universe);
    
    testCaseScaffolding.runBeforeTest();
    
    Assert.assertTrue(universe.currentTestDescriptionThreadLocal.get() != null);
    
    ((MyTest)testDescription.getTestCase()).accepter.addTearDown(new TearDown() {
      
      public void tearDown() throws Exception {
        throw new RuntimeException();
      }
    });
    
    try {
      testCaseScaffolding.runAfterTest();
      Assert.fail();
    } catch (RuntimeException good) {}
    Assert.assertEquals(null, universe.currentTestDescriptionThreadLocal.get());
  }
  
  @Test public void testFailingTestDoesNotSpoilThreadLocal() {
    GuiceBerryEnvSelector guiceBerryEnvSelector = 
      DefaultEnvSelector.of(MyGuiceBerryEnvThatThrowsOnTestWrapperBeforeTest.class);
    TestDescription testDescription = bogusTestDescription();
    GuiceBerryUniverse.TestCaseScaffolding testCaseScaffolding = 
      new GuiceBerryUniverse.TestCaseScaffolding(testDescription, guiceBerryEnvSelector, universe);
    
    Assert.assertEquals(false, MyGuiceBerryEnvThatThrowsOnTestWrapperBeforeTest.beforeTestTearDownHasRun);
    
    try {
      testCaseScaffolding.runBeforeTest();
      Assert.fail();
    } catch (RuntimeException good) {
      Assert.assertEquals("kaboom", good.getMessage());
    }
    
    testCaseScaffolding.runAfterTest();
    
    Assert.assertEquals("The thread local tear down must be done even if the"
        + "TestWrapper fails.", null, universe.currentTestDescriptionThreadLocal.get());
    Assert.assertEquals(true, MyGuiceBerryEnvThatThrowsOnTestWrapperBeforeTest.beforeTestTearDownHasRun);
  }
  
  private static final class MyGuiceBerryEnvThatThrowsOnTestWrapperBeforeTest extends AbstractModule {
    
    private static boolean beforeTestTearDownHasRun = false;
    
    private final GuiceBerryModule gbm;
    
    @Override
    protected void configure() {
      install(gbm);
    }
    
    @SuppressWarnings("unused")
    public MyGuiceBerryEnvThatThrowsOnTestWrapperBeforeTest() {
      this.gbm = new GuiceBerryModule(GuiceBerryUniverseTest.universe);
      clear();
    }
    
    private void clear() {
      beforeTestTearDownHasRun = false;
    }
    
    @SuppressWarnings("unused")
    @Provides
    TestWrapper getWrapper(final TearDownAccepter tearDownAccepter) {
      
      return new TestWrapper() {
        public void toRunBeforeTest() {
          tearDownAccepter.addTearDown(new TearDown() {
            public void tearDown() throws Exception {
              beforeTestTearDownHasRun = true;
            }
          });
          throw new RuntimeException("kaboom");
        }
      };
    }
  }

}
