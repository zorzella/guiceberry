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
import com.google.guiceberry.DefaultEnvSelector;
import com.google.guiceberry.util.AnnotationBasedGuiceBerryEnvSelector;

import junit.framework.TestCase;

/**
 * Like {@link ManualTearDownGuiceBerry#setUp(TestCase, Class)} but using
 * {@link AnnotationBasedGuiceBerryEnvSelector} instead of
 * {@link DefaultEnvSelector}.
 *
 * @see AnnotationBasedAutoTearDownGuiceBerry
 * 
 * @author Luiz-Otavio "Z" Zorzella
 */
public class AnnotationBasedManualTearDownGuiceBerry {

  /**
   * Sets up the {@code testCase} by leveraging the 
   * {@link AnnotationBasedGuiceBerryEnvSelector} and returns a {@link TearDown}
   * whose {@link TearDown#tearDown()} method must be manually called (thus the
   * "manual" moniker).
   */
  public static TearDown setUp(TestCase testCase) {
    return ManualTearDownGuiceBerry.setUp(
        testCase, AnnotationBasedGuiceBerryEnvSelector.INSTANCE);
  }
}
