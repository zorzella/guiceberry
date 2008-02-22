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

package com.google.inject.testing.guiceberry;

import com.google.common.base.Preconditions;

import junit.framework.TestCase;

//import net.jcip.annotations.Immutable;

import java.util.Random;

import javax.servlet.http.Cookie;

/**
 * A {@link TestId} is created by GuiceBerry (and made
 * available to @Inject in tests), and uniquely identify a test. This ID is
 * also set as a cookie so the server serving a request can be matched with 
 * the test that initiated this, thus making it possible to control (say) 
 * injections from within a Functional Test.
 * 
 * @author zorzella
 */
//@Immutable
public final class TestId implements Comparable<TestId>, CharSequence {

  private final String testCaseName;
  private final String testMethodName;
  // @VisibleForTesting
  public final long random;
  private final String asString;
  
  // @VisibleForTesting
  public TestId(TestCase testCase) {
    this(testCase, new Random().nextInt(1000));
  }

  // @VisibleForTesting
  public TestId(TestCase testCase, long random) {
    this(testCase.getClass().getName(), 
        testCase.getName(), 
        random);
  }
  
  // @VisibleForTesting
  TestId(String testCaseName, String testMethodName, long random) {
    this.testCaseName = testCaseName; 
    this.testMethodName = testMethodName;
    this.random = random;
    this.asString = getAsString(testCaseName, testMethodName, random);
  }

  private static String getAsString(String testCaseName, String testMethodName,
      long random) {
    return testMethodName + ":" + testCaseName + ":" + random;
  }

  public TestId(Cookie cookie) {
    String[] parts = cookie.getValue().split(":");
    Preconditions.checkState(parts.length == 3);
    this.testMethodName = parts[0];
    this.testCaseName = parts[1];
    this.random = Long.parseLong(parts[2]);
    this.asString = getAsString(testCaseName, testMethodName, random);
  }

  @Override
  public String toString() {
    return asString;
  }
  
  public int compareTo(TestId that) {
    return this.asString.compareTo(that.asString);
  }
  
  @Override
  public boolean equals(Object that) {
    if (!(that instanceof TestId)) {
      return false;
    }
    return this.asString.equals(((TestId)that).asString);
  }
  
  @Override
  public int hashCode() {
    return this.asString.hashCode();
  }

  public char charAt(int index) {
    return asString.charAt(index);
  }

  public int length() {
    return asString.length();
  }

  public CharSequence subSequence(int start, int end) {
    return asString.subSequence(start, end);
  }
}
