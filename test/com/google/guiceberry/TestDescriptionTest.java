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

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Luiz-Otavio "Z" Zorzella
 */
public class TestDescriptionTest {

  private static final Object BOGUS_TEST = "";
  
  @Test
  public void testValidName() {
    String validName = "valid.$_name0";
    TestDescription testDescription = new TestDescription(BOGUS_TEST, validName);
    Assert.assertEquals(validName, testDescription.getName());
  }
  
  @Test
  public void testInvalidName() {
    String invalidName = "in%%%%v___a    lid __     %^^^ name";
    TestDescription testDescription = new TestDescription(BOGUS_TEST, invalidName);
    Assert.assertEquals("in_v_a_lid_name", testDescription.getName());
  }
}
