/*
 * Copyright (C) 2012 Google Inc.
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

import com.google.inject.Guice;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jongerrish@google.com (Jonathan Gerrish)
 */
public class GuiceBerryModuleTest {

  /**
   * To allow for {@link GuiceBerryModule}s to be installed multiple times in
   * an injector, any two instances have to equal each other (which is to be the
   * case, except in case the package-private
   * {@link GuiceBerryModule#GuiceBerryModule(GuiceBerryUniverse)} constructor
   * is used, which is only the case in tests).
   */
  @Test
  public void moduleCanBeInstalledMultipleTimes() {
    Assert.assertEquals("See test javadoc.",
        new GuiceBerryModule(), new GuiceBerryModule());

    // This second test is not strictly necessary, but it serves as a reality
    // check. Guice should be able to create an injector with GuiceBerryModule
    // doubly installed, which should be always the case, since we just asserted
    // that GuiceBerryModules equal each other. 
    Guice.createInjector(new GuiceBerryModule(), new GuiceBerryModule());
    // If something is out of whack, an exception is thrown
  }
}
