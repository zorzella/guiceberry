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

import junit.framework.TestCase;

/**
 * @author Luiz-Otavio Zorzella
 */
class GuiceBerryEnvRemappersCoupler {

  private final GuiceBerryEnvRemapper oldRemapper;
  private final com.google.inject.testing.guiceberry.GuiceBerryEnvRemapper newRemapper;

  private GuiceBerryEnvRemappersCoupler(
     com.google.inject.testing.guiceberry.GuiceBerryEnvRemapper newRemapper,
     GuiceBerryEnvRemapper oldRemapper) {
    this.oldRemapper = oldRemapper;
    this.newRemapper = newRemapper;
  }

  static GuiceBerryEnvRemappersCoupler forNewRemappper(
      com.google.inject.testing.guiceberry.GuiceBerryEnvRemapper remapper) {
    return new GuiceBerryEnvRemappersCoupler(remapper, null);
  }
  
  static GuiceBerryEnvRemappersCoupler forOldRemappper(
      GuiceBerryEnvRemapper remapper) {
    return new GuiceBerryEnvRemappersCoupler(null, remapper);
  }

  public String remap(TestCase testCase, String declaredGbeName) {
    if (newRemapper != null) {
      return newRemapper.remap(testCase.getClass().getName(), declaredGbeName);
    } else if (oldRemapper != null) {
      System.err.println("You are using a deprecated GuiceBerryEnvRemapper interface. " +
      		"Please upgrade.");
      return oldRemapper.remap(testCase, declaredGbeName);
    }
    throw new IllegalStateException();
  }

  public Object backing() {
    if (newRemapper != null) {
      return newRemapper;
    } else if (oldRemapper != null) {
      return oldRemapper;
    }
    throw new IllegalStateException();
  }
}
