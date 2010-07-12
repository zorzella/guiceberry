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

package com.google.guiceberry;

import com.google.common.base.Preconditions;

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

  public static final String COOKIE_NAME = "testid";
  
  private final String name;
  private final long random;
  private final String asString;

  TestId(String name) {
    this(name, new Random().nextInt(1000));
  }

  private TestId(String name, long random) {
    this.name = name;
    this.random = random;
    this.asString = getAsString(name, random);
  }

  private static String getAsString(String name, long random) {
    return name + ":" + random;
  }

  public TestId(Cookie cookie) {
    String[] parts = cookie.getValue().split(":");
    Preconditions.checkState(parts.length == 2);
    this.name = parts[0];
    this.random = Long.parseLong(parts[1]);
    this.asString = getAsString(name, random);
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

  public com.google.inject.testing.guiceberry.TestId toDeprecatedTestId() {
    int dotIndex = name.lastIndexOf('.');
    String testCaseName = name.substring(0, dotIndex);
    String testMethodName = name.substring(dotIndex + 1);
    return new com.google.inject.testing.guiceberry.TestId(testCaseName, testMethodName, this.random);
  }
}
