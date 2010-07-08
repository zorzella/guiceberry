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
package com.google.guiceberry;

import com.google.inject.Module;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3;

/**
 * {@inheritDoc}
 * 
 * This is the default implementation of {@link EnvChooser}.
 * 
 * @author Luiz-Otavio "Z" Zorzella
 */
public class DefaultEnvChooser implements EnvChooser {

  private static final String OVERRIDE_SYSTEM_PROPERY_NAME = "EnvChooserOverride";
  
  private final String clazzName;

  private DefaultEnvChooser(String clazzName) {
    this.clazzName = clazzName;
  }

  public static EnvChooser of(Class<? extends Module> clazz) {
    return of(clazz.getName());
  }
  
  public static EnvChooser of(String clazzName) {
    EnvChooser override = getOverride();
    if (override != null) {
      return override;
    } else {
      return new DefaultEnvChooser(clazzName);
    }
  }
  
  public Class<? extends Module> guiceBerryEnvToUse(TestDescription testDescription) {
    EnvChooser override = getOverride();
    if (override != null) {
      return override.guiceBerryEnvToUse(testDescription);
    } else {
      return getGbeFromClazzName();
    }
  }
  
  @SuppressWarnings("unchecked")
  private static EnvChooser getOverride() {
    String overrideName = System.getProperty(DefaultEnvChooser.OVERRIDE_SYSTEM_PROPERY_NAME);
    if (overrideName != null) {
      Class clazz;
      try {
        clazz = GuiceBerryJunit3.class.getClassLoader().loadClass(overrideName);
        } catch (ClassNotFoundException e) {
          throw new IllegalArgumentException(String.format(
            "Class '%s' does not exist, and it is being declared as a '%s' override (though the '%s' System Property).",
            overrideName,
            DefaultEnvChooser.class.getName(),
            OVERRIDE_SYSTEM_PROPERY_NAME
            ), e);
        }
        if (EnvChooser.class.isAssignableFrom(clazz)) {
          return instantiateEnvChooser(clazz, overrideName);
        }
        throw new IllegalArgumentException(String.format(
          "Class '%s' is being declared as a GuiceBerryEnvRemapper, but does not implement that interface", 
          overrideName));
        
    }
    return null;
  }

  private static EnvChooser instantiateEnvChooser(
      Class<? extends EnvChooser> clazz, String overrideName) {
    try {
      return clazz.getConstructor().newInstance();
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(String.format(
        "GuiceBerryEnvRemapper '%s' must have public zero-arguments constructor", 
        overrideName), e);
    } catch (Exception e) {
      throw new RuntimeException(String.format(
        "There was a problem trying to instantiate your GuiceBerryEnvRemapper '%s'", overrideName), 
        e);
    }
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Module> getGbeFromClazzName() {
    Class<?> temp = getGbeClassFromClassName(clazzName);
    
    if (!Module.class.isAssignableFrom(temp)) {
      String msg = String.format(
        "The '%s' class must be a Guice Module (i.e. implement com.google.inject.Module).", 
        clazzName);
      throw new IllegalArgumentException(msg);
    }
      
    return (Class<? extends Module>) temp;
  }

  static Class<?> getGbeClassFromClassName(String gbeName) {
    Class<?> className;
    try {
      className = GuiceBerryJunit3.class.getClassLoader().loadClass(gbeName);   
    } catch (ClassNotFoundException e) {  
      String msg = String.format(
              "Class '%s' was not found.",
              gbeName.toString());
      throw new IllegalArgumentException(msg, e);
    }
    return className;
  }
}
