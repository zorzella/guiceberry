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

import com.google.common.collect.Lists;
import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.ConfigurationException;
import com.google.inject.CreationException;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
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

  /**
   * This test makes sure that if the test has a missing binding, GuiceBerry
   * will fail before it runs the {@link GuiceBerryEnvMain#run()} method.
   */
  @Test public void testFailsInjectionBeforeRunningGuiceBerryEnvMain_MissingTestBinding() {
    GuiceBerryEnvSelector guiceBerryEnvSelector = 
      DefaultEnvSelector.of(MyGuiceBerryEnvWithGuiceBerryEnvMainThatThrows.class);
    TestDescription testDescription = new TestDescription(new ClassWithUnsatisfiedDependency(), "bogus test case");
    GuiceBerryUniverse.TestCaseScaffolding testCaseScaffolding = 
      new GuiceBerryUniverse.TestCaseScaffolding(testDescription, guiceBerryEnvSelector, universe);
      
    try {
      testCaseScaffolding.runBeforeTest();
      Assert.fail("The test has an unsatisfied injection, and the GuiceBerryEnvMain "
          + "throws an Exception. Either of these reasons should have prevented the "
          + "test from having gotten here.");
    } catch (MyGuiceBerryEnvWithGuiceBerryEnvMainThatThrows.GuiceBerryEnvMainWasExecutedException toThrow) {
      throw toThrow;
    } catch (RuntimeException maybeExpected) {
      Assert.assertEquals(ConfigurationException.class, maybeExpected.getCause().getClass());
    }
      
    testCaseScaffolding.runAfterTest();
  }

  /**
   * Like {@link #testFailsInjectionBeforeRunningGuiceBerryEnvMain_MissingTestBinding()}
   * except for bindings that are missing in the test wrapper.
   */
  @Test public void testFailsInjectionBeforeRunningGuiceBerryEnvMain_MissingWrapperBinding() {
    GuiceBerryEnvSelector guiceBerryEnvSelector = 
      DefaultEnvSelector.of(MyGuiceBerryEnvWithGuiceBerryEnvMainThatThrowsAndWithATestWrapperWithAMissingBinding.class);
    TestDescription testDescription = new TestDescription(new MyTest(), "some test case");
    GuiceBerryUniverse.TestCaseScaffolding testCaseScaffolding = 
      new GuiceBerryUniverse.TestCaseScaffolding(testDescription, guiceBerryEnvSelector, universe);
      
    try {
      testCaseScaffolding.runBeforeTest();
      Assert.fail("The test has an unsatisfied injection, and the GuiceBerryEnvMain "
          + "throws an Exception. Either of these reasons should have prevented the "
          + "test from having gotten here.");
    } catch (MyGuiceBerryEnvWithGuiceBerryEnvMainThatThrows.GuiceBerryEnvMainWasExecutedException toThrow) {
      throw toThrow;
    } catch (CreationException expected) {}
      
    testCaseScaffolding.runAfterTest();
  }
  
  private interface UnsatisfiedDependency {}
  
  private static final class ClassWithUnsatisfiedDependency {
    @SuppressWarnings("unused")
    @Inject UnsatisfiedDependency unsatisfied;
  }
  
  private static final class MyGuiceBerryEnvWithGuiceBerryEnvMainThatThrows extends AbstractModule {

    private static final class GuiceBerryEnvMainWasExecutedException extends RuntimeException {
      public GuiceBerryEnvMainWasExecutedException() {
        super("GuiceBerryEnvMain was executed");
      }
    }
    
    private final GuiceBerryModule gbm;
    
    @Override
    protected void configure() {
      install(gbm);
    }
    
    @SuppressWarnings("unused")
    public MyGuiceBerryEnvWithGuiceBerryEnvMainThatThrows() {
      this.gbm = new GuiceBerryModule(GuiceBerryUniverseTest.universe);
    }
    
    @Provides
    GuiceBerryEnvMain getMain() {
      return new GuiceBerryEnvMain() {
        public void run() {
          throw new GuiceBerryEnvMainWasExecutedException();
        }
      };
    }
  }
  
  /**
   * Like {@link MyGuiceBerryEnvWithGuiceBerryEnvMainThatThrows}, but also with
   * a {@link TestWrapper} that is missing a binding.
   */
  private static final class MyGuiceBerryEnvWithGuiceBerryEnvMainThatThrowsAndWithATestWrapperWithAMissingBinding 
      extends AbstractModule {

    private static final class GuiceBerryEnvMainWasExecutedException extends RuntimeException {
      public GuiceBerryEnvMainWasExecutedException() {
        super("GuiceBerryEnvMain was executed");
      }
    }
    
    private final GuiceBerryModule gbm;
    
    @Override
    protected void configure() {
      install(gbm);
    }
    
    @SuppressWarnings("unused")
    public MyGuiceBerryEnvWithGuiceBerryEnvMainThatThrowsAndWithATestWrapperWithAMissingBinding() {
      this.gbm = new GuiceBerryModule(GuiceBerryUniverseTest.universe);
    }
    
    @Provides
    GuiceBerryEnvMain getMain() {
      return new GuiceBerryEnvMain() {
        public void run() {
          throw new GuiceBerryEnvMainWasExecutedException();
        }
      };
    }
    
    @Provides
    TestWrapper buildTestWrapper(
        @SuppressWarnings("unused") UnsatisfiedDependency unsatisfiedDependency) {
      return new TestWrapper() {
        public void toRunBeforeTest() {}
      };
    }
  }

  /**
   * Makes sure that the {@link GuiceBerryEnvMain#run()} method is called before
   * any provision happens (either in the test or the {@link TestWrapper}.
   */
  @Test public void testEnsureCorrectOrderOfBootstrap() {
    GuiceBerryEnvSelector guiceBerryEnvSelector = 
      DefaultEnvSelector.of(MyGuiceBerryEnvWithGuiceBerryEnvMainThatKeepsTabsOnTheOrder.class);
    TestDescription testDescription = new TestDescription(new MyTestThatKeepsTabsOnTheOrder(), "some test case");
    GuiceBerryUniverse.TestCaseScaffolding testCaseScaffolding = 
      new GuiceBerryUniverse.TestCaseScaffolding(testDescription, guiceBerryEnvSelector, universe);
      
    testCaseScaffolding.runBeforeTest();
      
    testCaseScaffolding.runAfterTest();
    
    Assert.assertEquals("GuiceBerryEnvMain",
        MyGuiceBerryEnvWithGuiceBerryEnvMainThatKeepsTabsOnTheOrder.eventsInOrder.get(0));
  }

  /**
   * Like {@link MyGuiceBerryEnvWithGuiceBerryEnvMainThatThrows}, but also with
   * a {@link TestWrapper} that is missing a binding.
   */
  private static final class MyGuiceBerryEnvWithGuiceBerryEnvMainThatKeepsTabsOnTheOrder
      extends AbstractModule {

    private static final List<String> eventsInOrder = Lists.newArrayList();
    
    private final GuiceBerryModule gbm;
    
    @Override
    protected void configure() {
      install(gbm);
    }
    
    @SuppressWarnings("unused")
    public MyGuiceBerryEnvWithGuiceBerryEnvMainThatKeepsTabsOnTheOrder() {
      this.gbm = new GuiceBerryModule(GuiceBerryUniverseTest.universe);
    }
    
    @Provides
    GuiceBerryEnvMain getMain() {
      return new GuiceBerryEnvMain() {
        public void run() {
          eventsInOrder.add("GuiceBerryEnvMain");
        }
      };
    }
    
    @Provides
    TestWrapper buildTestWrapper() {
      eventsInOrder.add("buildTestWrapper");

      return new TestWrapper() {
        public void toRunBeforeTest() {}
      };
    }
  }

  private static final class MyClassThatKeepsTabsOnTheOrder {
    @Inject
    public MyClassThatKeepsTabsOnTheOrder() {
      MyGuiceBerryEnvWithGuiceBerryEnvMainThatKeepsTabsOnTheOrder.eventsInOrder
        .add("MyClassThatKeepsTabsOnTheOrder");
    }
  }
  
  private static final class MyTestThatKeepsTabsOnTheOrder {
    @SuppressWarnings("unused")
    @Inject
    MyClassThatKeepsTabsOnTheOrder myClass;
  }
}
