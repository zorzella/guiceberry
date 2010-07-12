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

import com.google.common.annotations.VisibleForTesting;
import com.google.guiceberry.DefaultEnvSelector;
import com.google.guiceberry.GuiceBerryEnvSelector;
import com.google.guiceberry.TestDescription;
import com.google.guiceberry.junit3.AnnotationBasedAutoTearDownGuiceBerry;
import com.google.guiceberry.junit3.AnnotationBasedManualTearDownGuiceBerry;
import com.google.inject.Module;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;

/**
 * A {@link GuiceBerryEnvSelector} that is based on the {@link AnnotatedGuiceBerryEnv}
 * annotation, though it also honors the {@link DefaultEnvSelector}'s override.
 *
 * @see AnnotationBasedManualTearDownGuiceBerry
 * @see AnnotationBasedAutoTearDownGuiceBerry
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public class AnnotationBasedGuiceBerryEnvSelector implements GuiceBerryEnvSelector {

  public static final GuiceBerryEnvSelector INSTANCE = new AnnotationBasedGuiceBerryEnvSelector();
  public Class<? extends Module> guiceBerryEnvToUse(TestDescription testDescription) {
    String gbeName = getGbeNameFromGbeAnnotation(testDescription);
    
    if (DefaultEnvSelector.isOverridden()) {
      return DefaultEnvSelector.of(gbeName).guiceBerryEnvToUse(testDescription);
    }

    Class<? extends Module> gbeClass = getGbeClassFromClassName(gbeName);
    if (!Module.class.isAssignableFrom(gbeClass)) {
      String annotationName;
      if (isOldAnnotation(testDescription.getTestCaseClass())) {
        annotationName = GuiceBerryEnv.class.getSimpleName();
      } else {
        annotationName = AnnotatedGuiceBerryEnv.class.getSimpleName();
      }
      String msg = String.format(
          "Your @%s class '%s' must be a Guice Module (i.e. implement com.google.inject.Module).", 
          annotationName,
          gbeClass.getName()); 
      throw new IllegalArgumentException(msg);
    }
    return gbeClass;
  }
  
  @VisibleForTesting
  static String getGbeNameFromGbeAnnotation(TestDescription testDescription) {
    Class<?> testCaseClass = testDescription.getTestCaseClass();
    GuiceBerryEnv gbeAnnotation = getGbeAnnotation(testCaseClass);
    AnnotatedGuiceBerryEnv annotatedGbeAnnotation = getAnnotatedGbeAnnotation(testCaseClass);

    if ((gbeAnnotation != null) && (annotatedGbeAnnotation != null)) {
      throw new IllegalArgumentException("It seems your test used both the "
          + "deprecated GuiceBerryEnv and AnnotatedGuiceBerryEnv annotations."
          + "Please remove the deprecated one.");
    }

    String declaredGbeName;
    if (gbeAnnotation != null) {
      declaredGbeName = gbeAnnotation.value();
    } else if (annotatedGbeAnnotation != null) {
      declaredGbeName = annotatedGbeAnnotation.value();
    } else {
      throw new IllegalArgumentException(String.format(
          "In order to use the deprecated GuiceBerryJunit3, your test class "
          + "must have a @GuiceBerryEnv annotation. Either add one, or, better "
          + "yet, upgrade your code to make use of the GuiceBerry 3.0 adapters. "
          + DefaultEnvSelector.LINK_TO_UPGRADING_DOC
          ));
    }
    return declaredGbeName;
  }

  private static boolean isOldAnnotation(Class<?> testCaseClass) {
    return getGbeAnnotation(testCaseClass) != null;
  }
  
  private static AnnotatedGuiceBerryEnv getAnnotatedGbeAnnotation(Class<?> testCaseClass) { 
    AnnotatedGuiceBerryEnv gbeAnnotation =
      testCaseClass.getAnnotation(AnnotatedGuiceBerryEnv.class);
    return gbeAnnotation;
  }
  
  private static GuiceBerryEnv getGbeAnnotation(Class<?> testCaseClass) { 
    GuiceBerryEnv gbeAnnotation =
      testCaseClass.getAnnotation(GuiceBerryEnv.class);
    return gbeAnnotation;
  }  

  @SuppressWarnings("unchecked")
  private static Class<? extends Module> getGbeClassFromClassName(String gbeName) {
    Class<?> className;
    try {
      className = AnnotationBasedGuiceBerryEnvSelector.class.getClassLoader().loadClass(gbeName);   
    } catch (ClassNotFoundException e) {  
      String msg = String.format(
              "Class '%s' was not found.",
              gbeName.toString());
      throw new IllegalArgumentException(msg, e);
    }
    return (Class<? extends Module>) className;
  }
}
