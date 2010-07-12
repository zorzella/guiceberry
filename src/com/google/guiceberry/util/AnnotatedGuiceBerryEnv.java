/*
 * Copyright (C) 2010 Google Inc.
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

package com.google.guiceberry.util;

import com.google.guiceberry.DefaultEnvSelector;
import com.google.guiceberry.junit3.AnnotationBasedAutoTearDownGuiceBerry;
import com.google.guiceberry.junit3.AnnotationBasedManualTearDownGuiceBerry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * As an alternative to passing the GuiceBerry Env as a parameter to, say,
 * {@link DefaultEnvSelector}, you may use 
 * {@link AnnotationBasedAutoTearDownGuiceBerry} or
 * {@link AnnotationBasedManualTearDownGuiceBerry}, and annotate each test with
 * this @interface.
 * 
 * <p>The given string parameter to this annotation is expected to be the 
 * fully-qualified name of the GuiceBerry Env.
 * 
 * @author Luiz-Otavio Zorzella
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotatedGuiceBerryEnv {

  String value();
}
