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
package com.google.inject.testing.guiceberry.junit3.util;

import com.google.common.base.Preconditions;
import com.google.common.testing.TearDownAccepter;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.InjectionController;
import com.google.inject.testing.guiceberry.InjectionControllerProvider;
import com.google.inject.testing.guiceberry.TestId;

import junit.framework.TestCase;

/**
 * A Guice {@link com.google.inject.Module} that can be installed in a 
 * {@link GuiceBerryEnv} that makes {@link InjectionController}s available to be 
 * injected in tests.
 * 
 * @author zorzella
 */
public class InjectionControllerModule extends AbstractModule {

  private static class InjectionControllerGuiceBerryProvider 
    implements Provider<InjectionController>{

    @Inject
    private Provider<TestId> testIdProvider;

    @Inject
    private Provider<TestCase> testCaseProvider;
    
    public InjectionController get() {
      TestCase testCase = testCaseProvider.get();
      Preconditions.checkState(testCase instanceof TearDownAccepter, 
          "Test '%s' must be a TearDownAccepter to use controllable injection", 
          testCase);
      return InjectionControllerProvider.forTest(
          testIdProvider.get(), 
          (TearDownAccepter)testCase);
    }
  }

  @Override
  public void configure() {
    bind(InjectionController.class)
      .toProvider(InjectionControllerGuiceBerryProvider.class);
  }
}
