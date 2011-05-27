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

import com.google.common.testing.junit3.TearDownTestCase;

/**
 * This is merely a convenience class. If a test case extends this instead of
 * {@link TearDownTestCase}, it inherits the call to 
 * {@link GuiceBerryJunit3#setUp(junit.framework.TestCase)}. In fact, if the 
 * test does not need any further setup (which is quite likely), it would not
 * need to have a {@code setUp} method at all.
 * 
 * @author Luiz-Otavio Zorzella
 */
public class GuiceBerryJunit3TestCase extends TearDownTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    GuiceBerryJunit3.setUp(this);
  }
}
