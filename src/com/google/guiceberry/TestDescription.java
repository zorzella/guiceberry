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

import com.google.common.base.Objects;

/**
 * You won't have to deal with this class unless you are writing a test
 * framework adapter.
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public final class TestDescription {

  private final Object testCase;
  private final String name;
  private final TestId testId;

  /**
   * @param testCase
   * @param name
   */
  public TestDescription(Object testCase, String name, TestId testId) {
    this.testCase = testCase;
    this.name = name;
    this.testId = testId;
  }
  
  /**
   * @return the testCase
   */
  public Object getTestCase() {
    return testCase;
  }
  
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
 
  /**
   * @return the testId
   */
  public TestId getTestId() {
    return testId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof TestDescription)) {
      return false;
    }
    TestDescription that = (TestDescription)obj;
    return
      Objects.equal(this.testCase, that.testCase) &&
      Objects.equal(this.name, that.name) &&
      Objects.equal(this.testId, that.testId);
  }
 
  @Override
  public int hashCode() {
    return testId.hashCode();
  }
  
  @Override
  public String toString() {
    return getName();
  }
}