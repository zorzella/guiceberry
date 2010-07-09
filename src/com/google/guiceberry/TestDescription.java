// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.guiceberry;

import com.google.common.base.Objects;

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
      Objects.equal(this.name, that.name);
  }
  
  @Override
  public String toString() {
    return getName();
  }
}