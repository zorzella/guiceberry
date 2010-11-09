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
import com.google.common.base.Preconditions;

import java.util.regex.Pattern;

/**
 * This class is a test-framework-agnostic way of representing a test.
 * 
 * <p>See {@link #getName()} and {@link #getTestCaseClass()}.
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public final class TestDescription {

  private static final String NAME_ACCEPTABLE_CHARS_EXCEPT_UNDERSCORE = "A-Za-z0-9.$";
  public static final Pattern NAME_PATTERN = 
    Pattern.compile("[" + NAME_ACCEPTABLE_CHARS_EXCEPT_UNDERSCORE + "_]+");
  private static final Pattern REVERSE_NAME_PATTERN = 
    Pattern.compile("[^" + NAME_ACCEPTABLE_CHARS_EXCEPT_UNDERSCORE + "]+");
  
  private final Object testCase;
  private final String name;
  private final TestId testId;

  /**
   * You won't have to create an instance of this class unless you are writing a
   * test framework adapter.
   *
   * <p>The given {@code testCase} is used for two main purposes: it is the
   * class that GuiceBerry will {@link com.google.inject.Injector#injectMembers(Object)}
   * and its class is exposed by the public {@link #getTestCaseClass()} method.
   * 
   * <p>The given {@code name}, on the other hand, is just a humanly-readable
   * name for the test. It is used in error messages, and it is also the name
   * of the {@link TestId} for that test. This name is mangled so that all
   * characters match the {@link #NAME_PATTERN} regexp. This pattern is
   * somewhat (though not completely) arbitrary -- file a bug if you think it
   * should be broader.
   */
  public TestDescription(Object testCase, String name) {
    this.testCase = Preconditions.checkNotNull(testCase);
    this.name = mangle(Preconditions.checkNotNull(name));
    this.testId = new TestId(name);
  }
  
  private static String mangle(String name) {
    return REVERSE_NAME_PATTERN.matcher(name).replaceAll("_");
  }
  
  public Class<?> getTestCaseClass() {
    return testCase.getClass();
  }
  
  /**
   * Returns the name of this test.
   */
  public String getName() {
    return name;
  }
 
  Object getTestCase() {
    return testCase;
  }
  
  TestId getTestId() {
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