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

package com.google.guiceberry;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.common.testing.TearDownStack;
import com.google.guiceberry.GuiceBerryUniverse;
import com.google.guiceberry.TestScope;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;

import junit.framework.TestCase;

/**
 * This Module provides the three basic bindings required by 
 * {@link GuiceBerryJunit3}, namely {@link TestId}, 
 * {@link TestCase} and the {@link TestScoped} scope. Without these three
 * bindinds, {@link GuiceBerryJunit3#setUp(TestCase)} will fail.

 * <p>Thus, this module is all but required to be installed by all 
 * {@link GuiceBerryEnv}s using JUnit3. 
 * 
 * <p>The only alternative would be to provide the same bindings through some
 * other means.
 * 
 * @see GuiceBerryEnv
 * 
 * @author Luiz-Otavio Zorzella
 */
public class GuiceBerryModule extends AbstractModule {
    
  protected final GuiceBerryUniverse universe;
  private final TestScope testScope;
  
  public GuiceBerryModule() {
    this(GuiceBerryUniverse.INSTANCE);
  }
  
  protected GuiceBerryModule(GuiceBerryUniverse universe) {
    this.universe = universe;
    this.testScope = new TestScope(universe);
  }
  
  @Override
  public void configure() {
    bind(TestScope.class).toInstance(testScope);
    bindScope(TestScoped.class, testScope);
    bindScope(com.google.inject.testing.guiceberry.TestScoped.class, testScope);
    bind(TearDownAccepter.class).to(ToTearDown.class);
  }

  @Provides
  @TestScoped
  ToTearDown getToTearDown() {
    return new ToTearDown() {
      TearDownStack delegate = new TearDownStack();
      
      public void addTearDown(TearDown tearDown) {
        delegate.addTearDown(tearDown);
      }
    
      public void runTearDown() {
        delegate.runTearDown();
      }
    };
  }
  
  @Provides
  @TestScoped
  TestId getTestId() {
    return universe.currentTestDescriptionThreadLocal.get().getTestId();
  }
  
  public interface ToTearDown extends TearDownAccepter {
    void runTearDown();
  }
}
