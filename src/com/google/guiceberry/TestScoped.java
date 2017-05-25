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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.Scope;
import com.google.inject.ScopeAnnotation;

/**
 * This defines a {@link Scope} that begins during test "set up" and ends during
 * test "tear down". Creates no more than one object per test thread and its
 * children threads during a test run. When two tests share a thread pool
 * exception will be thrown when the thread is reused as there is no way to
 * determine a parent test when several tests are run in parallel. You may
 * explicitly reset a thread's state by using
 * {@link TestScope#setInternalStateForCurrentThread}.
 * 
 * @see TestScope implementation details
 * 
 * @author Luiz-Otavio "Z" Zorzella
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface TestScoped {}
