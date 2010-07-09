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
 * Required binding for a "main" {@link GuiceBerryEnv}. Upon every test's
 * start and exit (in JUnit setUp and tearDown), this listener is notified.
 *
 * By "main" {@link GuiceBerryEnv} it is understood the module defined by the 
 * user  which is provided by {@link GuiceBerryEnv} annotation. 
 *
 * @see GuiceBerryEnv
 * 
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
 */
public interface TestScopeListener { 

  /** Performs all the operations needed when the test enters a scope*/
  void enteringScope();
  
  /** Performs all the operations needed when the test exits a scope*/
  void exitingScope();
  
}
