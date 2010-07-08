// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.guiceberry;

import com.google.common.base.Objects;

public final class TestDescription {

  final Object testCase;
  final String name;

  /**
   * @param testCase
   * @param name
   */
  public TestDescription(Object testCase, String name) {
    this.testCase = testCase;
    this.name = name;
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