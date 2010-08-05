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

/**
 * If a {@link TestWrapper} is bound in your GuiceBerry Env, 
 * {@link #toRunBeforeTest} is called before each test is executed, and 
 * {@link #toRunAfterTest()} is called after each test is executed.
 *
 * <p>Both methods are guaranteed to be executed at a time when all Injections
 * are still valid (as far as GuiceBerry is concerned), and the test is still
 * in scope (i.e. {@link TestScoped} is valid.
 *
 * <p>See an usage example of this at TODO
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public interface TestWrapper { 

  /** @see TestWrapper */
  void toRunBeforeTest();
  
  /** @see TestWrapper */
  void toRunAfterTest();
}
