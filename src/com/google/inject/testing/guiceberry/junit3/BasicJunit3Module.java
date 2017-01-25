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

import com.google.guiceberry.DeprecatedGuiceBerryModule;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.TestScoped;

import junit.framework.TestCase;

/**
 * This Module provides the three basic bindings required by 
 * {@link GuiceBerryJunit3}, namely {@link TestId}, 
 * {@link TestCase} and the {@link TestScoped} scope. Without these three
 * bindings, {@link GuiceBerryJunit3#setUp(TestCase)} will fail.

 * <p>Thus, this module is all but required to be installed by all 
 * {@link GuiceBerryEnv}s using JUnit3. 
 * 
 * <p>The only alternative would be to provide the same bindings through some
 * other means.
 * 
 * @see GuiceBerryEnv
 * 
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
 */
public class BasicJunit3Module extends DeprecatedGuiceBerryModule {
}
