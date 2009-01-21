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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.Scope;
import com.google.inject.ScopeAnnotation;

/**
 * This defines a {@link Scope} that lasts for a single test.
 * 
 * <p>A test conceptually comes in scope when it starts and goes out of scope
 * when it finishes its execution (e.g., on JUnit lingo, roughly at the moment 
 * of {@link junit.framework.TestCase#setUp()} and 
 * {@link junit.framework.TestCase#tearDown()}). 
 *  
 * @see com.google.inject.testing.guiceberry.junit3.JunitTestScope for the JUnit-specific implementation of this scope
 * 
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface TestScoped {}
