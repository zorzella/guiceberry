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

import com.google.common.testing.TearDownAccepter;

/**
 * If a {@link TestWrapper} is bound in your GuiceBerry Env, 
 * {@link #toRunBeforeTest} is called before each test is executed. To perform
 * operations after the test, use an injected {@link TearDownAccepter}.
 *
 * <p>This method (and the added tear downs) are guaranteed to be executed at a
 * time when all Injections are still valid (as far as GuiceBerry is concerned),
 * and the test is still in scope (i.e. {@link TestScoped} is valid.
 *
 * <p>A simple usage of this class would look like this, in your GuiceBerry env:
 * 
 * <code>
 * @Provides
 * @Singleton
 * TestWrapper getTestWrapper(final TearDownAccepter tearDownAccepter) {
 *   return new TestWrapper() {
 *     public void toRunBeforeTest() {
 *       tearDownAccepter.addTearDown(new TearDown() {
 *         public void tearDown() throws Exception {
 *           // here goes the code to be executed after each test
 *         }
 *       });
 *     }
 *   }
 *   // here goes the code to be executed before each test
 * }
 * </code>
 * 
 * <p>See an example of this at {@link junit4.tutorial_0_basic.Example3TestWrapperTest}
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public interface TestWrapper { 

  /**@see TestWrapper */
  void toRunBeforeTest();
  
}
