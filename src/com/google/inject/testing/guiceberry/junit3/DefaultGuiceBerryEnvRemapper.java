package com.google.inject.testing.guiceberry.junit3;

import junit.framework.TestCase;

public class DefaultGuiceBerryEnvRemapper implements GuiceBerryEnvRemapper {

  public String remap(TestCase test, String env) {
    // TODO(zorzella): migrate all current code to use remappers, and kill this
    String override = System.getProperty(GuiceBerryJunit3.buildModuleOverrideProperty(env));
    if (override != null) {
      return override;
    }
    return env;
  }
}
