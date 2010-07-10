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
import com.google.guiceberry.GuiceBerryEnvSelector;
import com.google.guiceberry.TestDescription;
import com.google.inject.Module;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;

/**
 * A {@link GuiceBerryEnvSelector} that is based on the {@link GuiceBerryEnv}
 * annotation, though it also honors the {@link DefaultEnvSelector}'s override.
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public class AnnotationBasedSelector implements GuiceBerryEnvSelector {

  private final TestDescription testDescription;

  AnnotationBasedSelector(TestDescription testDescription) {
    this.testDescription = testDescription;
  }

  public Class<? extends Module> guiceBerryEnvToUse() {
    String gbeName = getGbeNameFromGbeAnnotation(testDescription);
    
    if (DefaultEnvSelector.isOverridden()) {
      return DefaultEnvSelector.of(gbeName).guiceBerryEnvToUse();
    }

    Class<? extends Module> gbeClass = getGbeClassFromClassName(gbeName);
    if (!Module.class.isAssignableFrom(gbeClass)) {
      String msg = String.format(
          "@%s class '%s' must be a Guice Module (i.e. implement com.google.inject.Module).", 
          GuiceBerryEnv.class.getSimpleName(),
          gbeClass.getName()); 
      throw new IllegalArgumentException(msg);
    }
    return gbeClass;
  }
  
  private static String getGbeNameFromGbeAnnotation(TestDescription testDescription) {
    Object testCase = testDescription.getTestCase();
    GuiceBerryEnv gbeAnnotation = getGbeAnnotation(testCase);
    if (gbeAnnotation == null) {
      throw new IllegalArgumentException(String.format(
          "In order to use the deprecated GuiceBerryJunit3, your test class "
          + "must have a @GuiceBerryEnv annotation. Either add one, or, better "
          + "yet, upgrade your code to make use of the GuiceBerry 3.0 adapters. "
          + DefaultEnvSelector.LINK_TO_UPGRADING_DOC
          ));
    }
    
    String declaredGbeName = gbeAnnotation.value();
    return declaredGbeName;
  }

  private static GuiceBerryEnv getGbeAnnotation(Object testCase) { 
    GuiceBerryEnv gbeAnnotation =
      testCase.getClass().getAnnotation(GuiceBerryEnv.class);
    return gbeAnnotation;
  }  

  @SuppressWarnings("unchecked")
  private static Class<? extends Module> getGbeClassFromClassName(String gbeName) {
    Class<?> className;
    try {
      className = DefaultEnvSelector.class.getClassLoader().loadClass(gbeName);   
    } catch (ClassNotFoundException e) {  
      String msg = String.format(
              "Class '%s' was not found.",
              gbeName.toString());
      throw new IllegalArgumentException(msg, e);
    }
    return (Class<? extends Module>) className;
  }
}
