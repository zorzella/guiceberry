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
package com.google.inject.testing.guiceberry.junit3;

import com.google.guiceberry.DefaultEnvSelector;
import com.google.guiceberry.GuiceBerryEnvSelector;
import com.google.guiceberry.TestDescription;
import com.google.guiceberry.util.AnnotatedGuiceBerryEnv;
import com.google.inject.Module;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;

import junit.framework.TestCase;

/**
 * @author Luiz-Otavio "Z" Zorzella
 */
@Deprecated
class VersionTwoBackwardsCompatibleGuiceBerryEnvSelector implements GuiceBerryEnvSelector {

  private static final GuiceBerryEnvRemapper
    DEFAULT_GUICE_BERRY_ENV_REMAPPER = new IdentityGuiceBerryEnvRemapper();

  public Class<? extends Module> guiceBerryEnvToUse(TestDescription testDescription) {
    String gbeName = getGbeNameFromGbeAnnotation(testDescription);
    
    if (DefaultEnvSelector.isOverridden(gbeName)) {
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
  
  private static String getGbeNameFromGbeAnnotation(TestDescription testDescription) {
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
    
    GuiceBerryEnvRemapper remapper = getRemapper(declaredGbeName);
    String result = remapper.remap(null, declaredGbeName);
    if (result == null) {
      throw new IllegalArgumentException(String.format(
          "The installed GuiceBerryEnvRemapper '%s' returned 'null' for the " +
          "'%s' test, which declares '%s' as its GuiceBerryEnv", 
          remapper.getClass().getName(),
          testDescription.getName(),
          declaredGbeName));
    }
    return result;
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
  private static GuiceBerryEnvRemapper getRemapper(String declaredGbeName) {
    String remapperName = System.getProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
    if (remapperName != null) {
      
      if (DefaultEnvSelector.isOverridden(declaredGbeName)) {
        throw new IllegalArgumentException(String.format(
            "Both the '%s' and the deprecated '%s' system properties are set. " +
            "To fix this, stop using the deprecated system property. " +
            DefaultEnvSelector.LINK_TO_UPGRADING_DOC,
            DefaultEnvSelector.buildSystemPropertyName(declaredGbeName),
            GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME));
      } else {
        System.out.println(String.format(
            "********* ATTENTION ***********\n" +
            "You are using the deprecated '%s' system property. This still "
            + "works, but you are encouraged to upgrade from using the old "
            + "Remapper paradigm to the new EnvChooser paradigm." +
            DefaultEnvSelector.LINK_TO_UPGRADING_DOC,
            GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME));
      }
      
      Class clazz;
      try {
        clazz = GuiceBerryJunit3.class.getClassLoader().loadClass(remapperName);
        } catch (ClassNotFoundException e) {
          throw new IllegalArgumentException(String.format(
            "Class '%s', which is being declared as a GuiceBerryEnvRemapper, does not exist.", remapperName), e);
        }
        if (GuiceBerryEnvRemapper.class.isAssignableFrom(clazz)) {
          return instantiateRemapper(clazz, remapperName);
        }
        throw new IllegalArgumentException(String.format(
          "Class '%s' is being declared as a GuiceBerryEnvRemapper, but does not implement that interface", 
          remapperName));
    }
    
    return DEFAULT_GUICE_BERRY_ENV_REMAPPER;
  }

  private static GuiceBerryEnvRemapper instantiateRemapper(
      Class<? extends GuiceBerryEnvRemapper> clazz, String remapperName) {
    try {
      return clazz.getConstructor().newInstance();
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(String.format(
        "GuiceBerryEnvRemapper '%s' must have public zero-arguments constructor", 
        remapperName), e);
    } catch (Exception e) {
      throw new RuntimeException(String.format(
        "There was a problem trying to instantiate your GuiceBerryEnvRemapper '%s'", remapperName), 
        e);
    }
  }
  
  /**
   * An "identity" remapper, that remaps a
   * {@link com.google.inject.testing.guiceberry.GuiceBerryEnv} to itself.
   * This remapper is installed by default.
   * 
   * {@inheritDoc}
   * 
   * @author Luiz-Otavio Zorzella
   */
  private static final class IdentityGuiceBerryEnvRemapper 
      implements GuiceBerryEnvRemapper {
    public String remap(TestCase test, String env) {
      return env;
    }
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends Module> getGbeClassFromClassName(String gbeName) {
    Class<?> className;
    try {
      className = VersionTwoBackwardsCompatibleGuiceBerryEnvSelector.class.getClassLoader().loadClass(gbeName);   
    } catch (ClassNotFoundException e) {  
      String msg = String.format(
              "Class '%s' was not found.",
              gbeName.toString());
      throw new IllegalArgumentException(msg, e);
    }
    return (Class<? extends Module>) className;
  }
}
