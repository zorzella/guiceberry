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
package com.google.inject.testing.guiceberry;

/**
 * A remapper can be used to instruct GuiceBerry to use a different 
 * {@link com.google.inject.testing.guiceberry.GuiceBerryEnv} than the 
 * one directly mapped by a given test case.
 * 
 * <p>To use a remapper, set the {@link #GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME}
 * property.
 * 
 * <p>The primary purpose for this is to allow for running tests under
 * different "integration" modes, as can be seen in the tutorial example
 * {@link tutorial_2_advanced.Example5Remapper}.
 * I.e., this allows one to run any given test against multiple
 * {@link com.google.inject.testing.guiceberry.GuiceBerryEnv}s.
 * 
 * <p>See {@link #remap(String, String)}
 * 
 * @author Luiz-Otavio Zorzella
 */
public interface GuiceBerryEnvRemapper {
  /**
   * The name of the {@code System} property used to tell GuiceBerry 
   * which {@link GuiceBerryEnvRemapper} (if any) to use.
   */
  String GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME = 
      "GuiceBerryEnvRemapper";

  /**
   * Returns the class name for the 
   * {@link com.google.inject.testing.guiceberry.GuiceBerryEnv} to be used in 
   * place of the given {@code guiceBerryEnvName} for the given {@code testCase}.
   */
  String remap(String testCase, String guiceBerryEnvName);
}
