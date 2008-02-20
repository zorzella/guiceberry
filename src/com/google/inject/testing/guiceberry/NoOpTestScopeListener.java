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

package com.google.inject.testing.guiceberry;


/**
 * This class is an implementation of the {@link TestScopeListener} interface.
 * It does nothing when object enters the scope and also does nothing 
 * when something exits the scope.
 *
 * <p> This class can be used as an auxiliary class. {@link GuiceBerryEnv} 
 * annotation provides a module that has to bind {@link TestScopeListener} 
 * to some implementation. If there is no need to bind {@link TestScopeListener},
 * then you bind it to {@link NoOpTestScopeListener}.
 *
 *@see TestScopeListener
 * 
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
*/
public final class NoOpTestScopeListener implements TestScopeListener {
  
  public void enteringScope() {
  }
  
  public void exitingScope() {
  }
}
