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

package com.google.inject.testing.guiceberry.junit3;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.common.testing.TearDownStack;

import junit.framework.TestCase;

/**
 * This is merely a convenience class. If a test case extends this instead of
 * {@code TearDownTestCase}, it inherits the call to
 * {@link GuiceBerryJunit3#setUp(junit.framework.TestCase)}. In fact, if the 
 * test does not need any further setup (which is quite likely), it would not
 * need to have a {@code setUp} method at all.
 * 
 * @author Luiz-Otavio Zorzella
 */
public class GuiceBerryJunit3TestCase extends TestCase implements TearDownAccepter {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    GuiceBerryJunit3.setUp(this);
  }

  // BELOW: an "inline" of the old "TearDownTestCase" methods (everything but
  // the constructors and setUp) so we can stop depending on the deprecated
  // test-libraries-for-java project.

  private final TearDownStack stack = new TearDownStack(true);

  /**
   * Registers a TearDown implementor which will be run during {@link #tearDown()}
   */
  public final void addTearDown(TearDown tearDown) {
    stack.addTearDown(tearDown);
  }

  @Override protected final void tearDown() {
    stack.runTearDown();
  }

  // Override to run setUp() inside the try block, not outside
  @Override public final void runBare() throws Throwable {
    try {
      setUp();
      runTest();
    } finally {
      tearDown();
    }
  }
}
