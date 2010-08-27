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

import com.google.common.testing.TearDown;
import com.google.common.testing.junit3.TearDownTestCase;

/**
 * @author Luiz-Otavio "Z" Zorzella
 */
public class DefaultEnvSelectorTest extends TearDownTestCase {

  private static final class MyEnvOne extends GuiceBerryModule {}
  private static final class MyEnvTwo extends GuiceBerryModule {}
  
  public void testOverride() {
    assertFalse(DefaultEnvSelector.isOverridden(MyEnvOne.class));
    assertEquals(null, DefaultEnvSelector.getOverride(MyEnvOne.class.getName()));
    GuiceBerryEnvSelector envSelector = DefaultEnvSelector.of(MyEnvOne.class);
    assertEquals(MyEnvOne.class, envSelector.guiceBerryEnvToUse(null));

    override();
    
    assertTrue(DefaultEnvSelector.isOverridden(MyEnvOne.class));
    assertEquals(MyEnvTwo.class, DefaultEnvSelector.getOverride(MyEnvOne.class.getName()));
    envSelector = DefaultEnvSelector.of(MyEnvOne.class);
    assertEquals(MyEnvTwo.class, envSelector.guiceBerryEnvToUse(null));
  }

  private void override() {
    addTearDown(new TearDown() {
      
      public void tearDown() throws Exception {
        DefaultEnvSelector.clearOverride(MyEnvOne.class);
      }
    });
    DefaultEnvSelector.override(MyEnvOne.class, MyEnvTwo.class);
  }
}
