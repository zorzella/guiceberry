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
import com.google.inject.testing.guiceberry.junit3.GuiceBerryEnvRemapper;

/**
 * {@inheritDoc}
 * 
 * This is the default implementation of {@link EnvChooser}. The GuiceBerry Env
 * to use is the class (or its name) given as a parameter to one the {@link #of}
 * static factory methods, except when the {@link #override} feature is used.
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public class DefaultEnvChooser implements EnvChooser {

  /**
   * 
   */
  public static final String LINK_TO_UPGRADING_DOC =
    "For more details, see http://guiceberry.googlecode.com, section 'Upgrading from 2.0 to 3.0'";

  public static final String OVERRIDE_SYSTEM_PROPERY_NAME = "GuiceBerry.EnvChooserOverride";
  
  private final String clazzName;

  private DefaultEnvChooser(String clazzName) {
    this.clazzName = clazzName;
  }

  /**
   * Specifies {@code clazz} as the GuiceBerry Env to use for a test, except
   * when the {@link #override} feature is used.
   *
   * @see #of(String)
   */
  public static EnvChooser of(Class<? extends Module> guiceBerryEnvClazz) {
    return of(guiceBerryEnvClazz.getName());
  }

  /**
   * Use this version of the static factory method instead of {@link #of(Class)}
   * if you wish to not have a compile-time dependency between your test and
   * your GuiceBerry Env. See TODO for more details.
   *
   * @see #of(Class)
   */
  public synchronized static EnvChooser of(String guiceBerryEnvClazzName) {
    EnvChooser override = getOverride();

    if (System.getProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME) != null) {
      System.out.println(String.format(
          "********* ATTENTION ***********\n" +
          "I see you have the deprecated '%s' system property set, which is. " +
          "honored anymore. " +
          LINK_TO_UPGRADING_DOC,
          GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME));
    }

    if (override != null) {
      return override;
    } else {
      return new DefaultEnvChooser(guiceBerryEnvClazzName);
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
      
      if (System.getProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME) != null) {
        throw new IllegalArgumentException(String.format(
            "Both the '%s' and the deprecated '%s' system properties are set. " +
            "To fix this, stop using the deprecated system property. " +
            LINK_TO_UPGRADING_DOC, 
            OVERRIDE_SYSTEM_PROPERY_NAME, 
            GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME));
      }
      
      Class clazz;
      try {
        clazz = DefaultEnvChooser.class.getClassLoader().loadClass(overrideName);
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
      className = DefaultEnvChooser.class.getClassLoader().loadClass(gbeName);   
    } catch (ClassNotFoundException e) {  
      String msg = String.format(
              "Class '%s' was not found.",
              gbeName.toString());
      throw new IllegalArgumentException(msg, e);
    }
    return className;
  }
  
  /**
   * The {@link DefaultEnvChooser} provides a simple mechanism for overriding
   * the {@link EnvChooser} returned by its {@link #of} methods: if a 
   * {@link System} property named {@link #OVERRIDE_SYSTEM_PROPERY_NAME} is set,
   * class whose name is the value for that property is used instead of 
   * {@link DefaultEnvChooser} (i.e. it is returned by the {@link #of} methods.
   *
   * <p>For details about this feature, see TODO
   *
   * <p>This method is a convenience method in case you wish to set that system
   * property programatically.
   *
   * <p>Note that this override is honored by code inside the {@link #of}
   * methods, so this {@link #override} method must be called <em>before</em>
   * calling those.
   * 
   * @throws IllegalArgumentException if you have either already called this
   *   method before, or otherwise set the {@link #OVERRIDE_SYSTEM_PROPERY_NAME}
   *   (say by passing a -D system property to the java runtime).
   */
  public synchronized void override(Class<? extends EnvChooser> envChooserOverride) {
    if (System.getProperty(DefaultEnvChooser.OVERRIDE_SYSTEM_PROPERY_NAME) != null) {
      throw new IllegalArgumentException();
    }
    System.setProperty(OVERRIDE_SYSTEM_PROPERY_NAME, envChooserOverride.getClass().getName());
  }
}
