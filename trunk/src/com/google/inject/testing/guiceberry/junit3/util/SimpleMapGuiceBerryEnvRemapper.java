/*
 * Copyright (C) 2008 Google Inc.
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
package com.google.inject.testing.guiceberry.junit3.util;

import com.google.inject.testing.guiceberry.junit3.GuiceBerryEnvRemapper;

import junit.framework.TestCase;

import java.util.Map;

/**
 * <p>See {@link GuiceBerryEnvRemapper}
 * 
 * Simple implementation of {@link GuiceBerryEnvRemapper} that simply does
 * a String->String mapping of an Env to another.
 * 
 * @author Luiz-Otavio Zorzella
 */
public class SimpleMapGuiceBerryEnvRemapper implements GuiceBerryEnvRemapper {

  private final Map<String, String> map;

  /**
   * The instance returned will simply map each key in the given {@code map}
   * to its value.
   */
  public SimpleMapGuiceBerryEnvRemapper(Map<String,String> map) {
    this.map = map;
  }
  
  public String remap(TestCase testCase, String guiceBerryEnvName) {
    return map.get(guiceBerryEnvName);
  }
}
