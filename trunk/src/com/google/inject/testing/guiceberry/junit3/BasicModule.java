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

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.TestScoped;

import junit.framework.TestCase;

/**
 * This Module can be installed within a {@link GuiceBerryEnv}.  
 * 
 * @see GuiceBerryEnv
 * 
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
 */
// TODO: rename to BasicJunit3Module
public class BasicModule extends AbstractModule {
    
      @Override
      public void configure() {
        final JunitTestScope testScope = new JunitTestScope();
        bindScope(TestScoped.class, testScope);
        bind(TestId.class).toProvider(new Provider<TestId>() {
          @Override
          public TestId get() {
            TestCase testClass = GuiceBerryJunit.getActualTestCase(); 
            return new TestId(testClass.getClass().getName(), testClass.getName());
          } 
        }).in(TestScoped.class);
        bind(TestCase.class).toProvider(new Provider<TestCase>() {
          @Override
          public TestCase get() {
            return GuiceBerryJunit.getActualTestCase();
          }
        }).in(TestScoped.class);
      }
}
