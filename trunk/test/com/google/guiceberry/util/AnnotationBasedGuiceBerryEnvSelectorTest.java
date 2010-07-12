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
package com.google.guiceberry.util;

import com.google.guiceberry.TestDescription;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Luiz-Otavio "Z" Zorzella
 */
public class AnnotationBasedGuiceBerryEnvSelectorTest {

  private static final Object oldTestCase = new OldTestCase();
  private static final Object newTestCase = new NewTestCase();
  private static final Object oldAndNewTestCase = new OldAndNewTestCase();

  @Test
  public void testOldAnnotation() {
    TestDescription testDescription = new TestDescription(oldTestCase, "oldTestCase");
    Assert.assertEquals(
        "old", 
        AnnotationBasedGuiceBerryEnvSelector.getGbeNameFromGbeAnnotation(testDescription));
  }
  
  @Test
  public void testNewAnnotation() {
    TestDescription testDescription = new TestDescription(newTestCase, "newTestCase");
    Assert.assertEquals(
        "new", 
        AnnotationBasedGuiceBerryEnvSelector.getGbeNameFromGbeAnnotation(testDescription));
  }

  @Test
  public void testOldAndNewAnnotationThrows() {
    TestDescription testDescription = new TestDescription(oldAndNewTestCase, "oldAndNewTestCase");
    try {
      AnnotationBasedGuiceBerryEnvSelector.getGbeNameFromGbeAnnotation(testDescription);
      Assert.fail();
    } catch (IllegalArgumentException good) {
    }
  }
  
  @GuiceBerryEnv("old")
  private static final class OldTestCase {}

  @AnnotatedGuiceBerryEnv("new")
  private static final class NewTestCase {}
  
  @GuiceBerryEnv("old")
  @AnnotatedGuiceBerryEnv("new")
  private static final class OldAndNewTestCase {}
  
}
