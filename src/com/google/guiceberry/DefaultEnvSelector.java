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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Module;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryEnvRemapper;

/**
 * See {@link GuiceBerryEnvSelector}.
 * 
 * <p>This is the default implementation of {@link GuiceBerryEnvSelector}. The GuiceBerry Env
 * to use is the class (or its name) given as a parameter to one the {@link #of}
 * static factory methods, except when the {@link #override} feature is used.
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public class DefaultEnvSelector implements GuiceBerryEnvSelector {

  public static final String LINK_TO_UPGRADING_DOC =
    "For more details, see http://guiceberry.googlecode.com, section 'Upgrading from 2.0 to 3.0'";

  private static final String OVERRIDE_SYSTEM_PROPERY_NAME = "GuiceBerryEnvSelectorOverride";
  
  private final String clazzName;

  private DefaultEnvSelector(String clazzName) {
    this.clazzName = clazzName;
  }

  /**
   * Specifies {@code clazz} as the GuiceBerry Env to use for a test, except
   * when the {@link #override} feature is used.
   *
   * @see #of(String)
   */
  public static GuiceBerryEnvSelector of(Class<? extends Module> guiceBerryEnvClazz) {
    return of(guiceBerryEnvClazz.getName());
  }

  /**
   * Use this version of the static factory method instead of {@link #of(Class)}
   * if you wish to not have a compile-time dependency between your test and
   * your GuiceBerry Env. See TODO for more details.
   *
   * @see #of(Class)
   */
  public synchronized static GuiceBerryEnvSelector of(String guiceBerryEnvClazzName) {
    Class<? extends Module> override = getOverride(guiceBerryEnvClazzName);

    if (System.getProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME) != null) {
      System.out.println(String.format(
          "********* ATTENTION ***********\n" +
          "I see you have the deprecated '%s' system property set, which is. " +
          "honored anymore. " +
          LINK_TO_UPGRADING_DOC,
          GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME));
    }

    if (override != null) {
      return new DefaultEnvSelector(override.getName());
    } else {
      return new DefaultEnvSelector(guiceBerryEnvClazzName);
    }
  }
  
  public Class<? extends Module> guiceBerryEnvToUse(TestDescription testDescription) {
    Class<? extends Module> result = getGbeFromClazzName();
    Class<? extends Module> override = getOverride(result.getName());
    if (override != null) {
      result = override;
    }
    return result;
  }
  
  @VisibleForTesting
  @SuppressWarnings("unchecked")
  static Class<? extends Module> getOverride(String guiceBerryEnvName) {
    String overrideName = getOverrideName(guiceBerryEnvName);
    if (overrideName != null) {
      
      if (System.getProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME) != null) {
        throw new IllegalArgumentException(String.format(
            "Both the '%s' and the deprecated '%s' system properties are set. " +
            "To fix this, stop using the deprecated system property. " +
            LINK_TO_UPGRADING_DOC, 
            OVERRIDE_SYSTEM_PROPERY_NAME, 
            GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME));
      }
      
      @SuppressWarnings("rawtypes")
      Class clazz;
      try {
        clazz = DefaultEnvSelector.class.getClassLoader().loadClass(overrideName);
        } catch (ClassNotFoundException e) {
          throw new IllegalArgumentException(String.format(
            "Class '%s' does not exist, and it is being declared as a '%s' override (though the '%s' System Property).",
            overrideName,
            DefaultEnvSelector.class.getName(),
            OVERRIDE_SYSTEM_PROPERY_NAME
            ), e);
        }
        if (Module.class.isAssignableFrom(clazz)) {
          return clazz;
        }
        throw new IllegalArgumentException(String.format(
          "Class '%s' is being declared as a GuiceBerryEnvSelector, but does not implement that interface", 
          overrideName));
        
    }
    return null;
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
      className = DefaultEnvSelector.class.getClassLoader().loadClass(gbeName);   
    } catch (ClassNotFoundException e) {  
      String msg = String.format(
              "Class '%s' was not found.",
              gbeName.toString());
      throw new IllegalArgumentException(msg, e);
    }
    return className;
  }
  
  /**
   * Overrides the {@code declaredGuiceBerryEnv} with the
   * {@code guiceBerryEnvOverride}.
   *
   * <p>The {@link DefaultEnvSelector} class provides a simple mechanism for
   * overriding a particular GuiceBerry Env with another. If, for a given
   * GuiceBerry Env, a {@link System} property named like the pattern in 
   * {@link #buildSystemPropertyName(String)} is set, the class whose name is
   * the value for that property is used instead of the declared GuiceBerry Env.
   *
   * <p>For details about this feature, see tutorial TODO
   *
   * <p>This method is a convenience method in case you wish to set that system
   * property programmatically.
   *
   * <p>Note that this override is honored by code inside the {@link #of}
   * methods, so this {@link #override} method must be called <em>before</em>
   * calling those.
   * 
   * @throws IllegalArgumentException if you have either already called this
   *   method before, or otherwise set the {@link #OVERRIDE_SYSTEM_PROPERY_NAME}
   *   (say by passing a -D system property to the java runtime).
   *   
   * @see #clearOverride(Class)
   */
  public static synchronized void override(
      Class<? extends Module> declaredGuiceBerryEnv,
      Class<? extends Module> guiceBerryEnvOverride) {
    if (isOverridden(declaredGuiceBerryEnv.getName())) {
      throw new IllegalArgumentException(String.format(
        "The GuiceBerry Env '%s' is already overridden by '%s'.", 
        declaredGuiceBerryEnv.getName(), getOverrideName(declaredGuiceBerryEnv.getName())));
    }
    System.setProperty(buildSystemPropertyName(declaredGuiceBerryEnv.getName()),
        guiceBerryEnvOverride.getName());
  }

  /**
   * Clears the {@link System} property override for the
   * {@code declaredGuiceBerryEnv}.
   */
  public static synchronized void clearOverride(
      Class<? extends Module> declaredGuiceBerryEnv) {
    System.clearProperty(buildSystemPropertyName(declaredGuiceBerryEnv.getName()));
  }

  /**
   * Returns true if the {@code declaredGuiceBerryEnv} is being overridden.
   */
  public static synchronized boolean isOverridden(
      Class<? extends Module> declaredGuiceBerryEnvClass) {
    return isOverridden(declaredGuiceBerryEnvClass.getName());
  }
  
  /**
   * Returns true if the {@code declaredGuiceBerryEnv} is being overridden.
   */
  public static synchronized boolean isOverridden(String declaredGuiceBerryEnvName) {
    return getOverrideName(declaredGuiceBerryEnvName) != null;
  }

  /**
   * Returns the name of the GuiceBerry Env that overrides
   * {@code declaredGuiceBerryEnvName}, or {@code null} if there is no override.
   */
  private static String getOverrideName(String declaredGuiceBerryEnvName) {
    return System.getProperty(buildSystemPropertyName(declaredGuiceBerryEnvName));
  }

  /**
   * Returns the name of the {@link System} property that, when set, will cause
   * {@link DefaultEnvSelector#of} to override the {@code declaredGuiceBerryEnv}
   * with the value of the {@link System} property.
   */
  public static String buildSystemPropertyName(String declaredGuiceBerryEnvName) {
    return OVERRIDE_SYSTEM_PROPERY_NAME + "_" + declaredGuiceBerryEnvName;
  }
}
