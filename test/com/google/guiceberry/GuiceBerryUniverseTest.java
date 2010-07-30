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
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;

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

  private static final class MyGuiceBerryEnv extends GuiceBerryModule {
    
    @SuppressWarnings("unused")
    public MyGuiceBerryEnv() {
      super(GuiceBerryUniverseTest.universe);
    }
  }
  
  @Before public void setUniverse () {
    if (universe != null) {
      throw new RuntimeException();
    }
    universe = new GuiceBerryUniverse();
  }

  @After public void resetUniverse() {
    universe = null;
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
    Assert.assertEquals(false, MyGuiceBerryEnvThatThrowsOnTestWrapperBeforeTest.afterTestHasRun);
    
    try {
      testCaseScaffolding.runBeforeTest();
      Assert.fail();
    } catch (RuntimeException good) {
      Assert.assertEquals("kaboom", good.getMessage());
    }
    
    try {
      testCaseScaffolding.runAfterTest();
      Assert.fail();
    } catch (RuntimeException good) {
      Assert.assertEquals("boomka", good.getMessage());
    }
    
    Assert.assertEquals("The thread local tear down must be done even if the"
        + "TestWrapper fails.", null, universe.currentTestDescriptionThreadLocal.get());
    Assert.assertEquals(true, MyGuiceBerryEnvThatThrowsOnTestWrapperBeforeTest.beforeTestTearDownHasRun);
    Assert.assertEquals(true, MyGuiceBerryEnvThatThrowsOnTestWrapperBeforeTest.afterTestHasRun);
  }
  
  private static final class MyGuiceBerryEnvThatThrowsOnTestWrapperBeforeTest extends GuiceBerryModule {
    
    private static boolean beforeTestTearDownHasRun = false;
    private static boolean afterTestHasRun = false;

    @SuppressWarnings("unused")
    public MyGuiceBerryEnvThatThrowsOnTestWrapperBeforeTest() {
      super(GuiceBerryUniverseTest.universe);
      clear();
    }
    
    private void clear() {
      beforeTestTearDownHasRun = false;
      afterTestHasRun = false;
    }
    
    @Provides
    @Singleton
    TestWrapper getWrapper(final TearDownAccepter accepter) {
      
      return new TestWrapper() {
        
        public void toRunBeforeTest() {
          accepter.addTearDown(new TearDown() {
            
            public void tearDown() throws Exception {
              beforeTestTearDownHasRun = true;
            }
          });
          throw new RuntimeException("kaboom");
        }
      
        public void toRunAfterTest() {
          afterTestHasRun = true;
          throw new RuntimeException("boomka");
        }
      };
    }
  }

}
