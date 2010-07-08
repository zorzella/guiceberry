// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.guiceberry;

import com.google.inject.Module;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryEnvRemapper;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3;

import junit.framework.TestCase;

import java.lang.annotation.Annotation;

/**
 * @author zorzella@google.com (Your Name Here)
 */
public class VersionTwoBackwardsCompatibleEnvChooser implements EnvChooser {

  private static final GuiceBerryEnvRemapper
    DEFAULT_GUICE_BERRY_ENV_REMAPPER = new IdentityGuiceBerryEnvRemapper();

  public Class<? extends Module> guiceBerryEnvToUse(TestDescription testDescription) {
    Object testCase = testDescription.getTestCase();
    if (isInOldVersion(testDescription)) {
      return oldGuiceBerryEnvToUse(testDescription);
    } else {
      return newGuiceBerryEnvToUse(testDescription);
    }
  }
  
  @Deprecated
  public Class<? extends Module> oldGuiceBerryEnvToUse(TestDescription testDescription) {
    String gbeName = getGbeNameFromGbeAnnotation(testDescription);
    @SuppressWarnings("unchecked")
    Class<? extends Module> gbeClass = 
      (Class<? extends Module>) DefaultEnvChooser.getGbeClassFromClassName(gbeName);
    if (!Module.class.isAssignableFrom(gbeClass)) {
      String msg = String.format(
          "@%s class '%s' must be a Guice Module (i.e. implement com.google.inject.Module).", 
          GuiceBerryEnv.class.getSimpleName(),
          gbeClass.getName()); 
      throw new IllegalArgumentException(msg);
    }
    
    if (hasAnnotation(testDescription, GuiceBerryEnvChooser.class)) {
      throw new RuntimeException();
    }
    
    return gbeClass;
  }
  
  public Class<? extends Module> newGuiceBerryEnvToUse(TestDescription testDescription) {
    Object testCase = testDescription.getTestCase();
    
    GuiceBerryEnvChooser gbeAnnotation =
      testCase.getClass().getAnnotation(GuiceBerryEnvChooser.class);
    
    if (gbeAnnotation == null) {
      throw new IllegalArgumentException(String.format(
        "Test class '%s' must have an @%s annotation.",
        testDescription.getTestCase().getClass().getName(), GuiceBerryEnvChooser.class.getSimpleName()));
    }
    
    Class<? extends EnvChooser> chooserClass = gbeAnnotation.value();
    
    EnvChooser envChooser = instantiateEnvChooser(chooserClass);
    Class<? extends Module> guiceBerryEnvToUse = envChooser.guiceBerryEnvToUse(testDescription);
    
    if (hasAnnotation(testDescription, GuiceBerryEnv.class)) {
      throw new RuntimeException();
    }
    
    return guiceBerryEnvToUse;
  }

  /**
   */
  private boolean isInOldVersion(TestDescription testDescription) {
    return hasAnnotation(testDescription, GuiceBerryEnv.class);
  }

  /**
   */
  private boolean hasAnnotation(TestDescription testDescription, Class<? extends Annotation> clazz) {
    return testDescription.getTestCase().getClass().getAnnotation(clazz) != null;
  }

  private EnvChooser instantiateEnvChooser(Class<? extends EnvChooser> chooserClass) {
    try {
      return chooserClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Deprecated
  private static String getGbeNameFromGbeAnnotation(TestDescription testDescription) {
    Object testCase = testDescription.getTestCase();
    String declaredGbeName = getGbeAnnotation(testCase).value();
    GuiceBerryEnvRemapper remapper = getRemapper();
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

  private static GuiceBerryEnv getGbeAnnotation(Object testCase) { 
    GuiceBerryEnv gbeAnnotation =
      testCase.getClass().getAnnotation(GuiceBerryEnv.class);
    return gbeAnnotation;
  }  

  @SuppressWarnings("unchecked")
  private static GuiceBerryEnvRemapper getRemapper() {
    String remapperName = System.getProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
    if (remapperName != null) {
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

}
