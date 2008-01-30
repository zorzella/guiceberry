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

import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * Each "test case" that wants GooseBerry injection must be annotated with this.
 * In JUnit lingo, this is a {@link junit.framework.TestCase}.
 * 
 * <p>The given string parameter to this annotation is expected to be a 
 * canonical name of a {@code Class<? extends Module>} that defines a "main" 
 * module for GooseBerry -- i.e. one that will be used to create an 
 * {@link com.google.inject.Injector}. 
 * 
 * 
 * @see GuiceBerryJunit 
 * 
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
 * 
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GuiceBerryEnv {

  String value();
}
