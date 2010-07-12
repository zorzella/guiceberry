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
package com.google.guiceberry.junit3;

import com.google.common.testing.TearDown;
import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.DefaultEnvSelector;
import com.google.guiceberry.util.AnnotationBasedGuiceBerryEnvSelector;

/**
 * Like {@link AutoTearDownGuiceBerry#setUp(TearDownTestCase, Class)} but using 
 * {@link AnnotationBasedGuiceBerryEnvSelector} instead of
 * {@link DefaultEnvSelector}.
 *
 * @see AnnotationBasedManualTearDownGuiceBerry
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public class AnnotationBasedAutoTearDownGuiceBerry {

  /**
   * Sets up the {@code testCase} by leveraging the 
   * {@link AnnotationBasedGuiceBerryEnvSelector} and automatically registers a 
   * {@link TearDown} (thus the "auto" moniker).
   */
  public static void setUp(TearDownTestCase testCase) {
    AutoTearDownGuiceBerry.setUp(testCase, AnnotationBasedGuiceBerryEnvSelector.INSTANCE);
  }
}
