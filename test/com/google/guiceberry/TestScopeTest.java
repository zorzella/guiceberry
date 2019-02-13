/*
 * Copyright (C) 2017 Google Inc.
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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Provider;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;
import com.google.inject.ProvisionException;

/**
 * Tests for {@link TestScope}.
 *
 * @author Timothy Basanov (timofey.basanov@gmail.com)
 */
public class TestScopeTest {

  /** Use default GuiceBerry configuration to get a {@link TestScoped} {@link TestId}. */
  @Rule public final GuiceBerryRule guiceBerryRule =
      new GuiceBerryRule(GuiceBerryModule.class);

  /** Directly injected value always matches the current test id. */
  @Inject TestId expectedTestId;
  /** Provider relies internally on a {@link TestScope}. */
  @Inject Provider<TestId> testIdProvider;
  /** {@link javax.inject.Singleton} {@link TestScope} itself. */
  @Inject TestScope testScope;

  /** Shorthand to read a test scoped test id from a different thread. */
  class GetProvidedTestId implements Callable<TestId> {
    public TestId call() throws Exception {
      return testIdProvider.get();
    }
  }

  /** Thread detached from any specific test. */
  static final ExecutorService detachedThread =
      Executors.newSingleThreadExecutor();
  static {
    detachedThread.execute(new Runnable() {
      public void run() {
        // task execution would enforce a thread creation outside of any test
      }
    });
  }

  /** Thread shared between two tests, should be created in the first test. */
  static final ExecutorService sharedThread =
      Executors.newSingleThreadExecutor();
  static final AtomicInteger sharedThreadTestCounter = new AtomicInteger(0);

  /** Verify that test id is scoped right within the main thread. */
  @Test
  public void testSameThreadSameTestId() throws Exception {
    TestId actualTestId =
        MoreExecutors.newDirectExecutorService().submit(new GetProvidedTestId()).get();
    Assert.assertSame(expectedTestId, actualTestId);
  }

  /** Threads created within a test inherit test scope. */
  @Test
  public void testChildThreadSameTestId() throws Exception {
    TestId actualTestId =
        Executors.newSingleThreadExecutor().submit(new GetProvidedTestId()).get();
    Assert.assertSame(expectedTestId, actualTestId);
  }

  /** Verify that detached thread does not share a test scope. */
  @Test
  public void testDetachedThreadNoTestId() throws Exception {
    try {
      detachedThread.submit(new GetProvidedTestId()).get();
      Assert.fail("Detached thread should not share a test scope");
    } catch (ExecutionException e) {
      Assert.assertSame(ProvisionException.class, e.getCause().getClass());
      Assert.assertSame(IllegalStateException.class, e.getCause().getCause().getClass());
      Assert.assertTrue(e.getCause().getCause().getMessage().contains("GuiceBerry"));
    }

    final TestScope.InternalState internalState =
        testScope.getInternalStateForCurrentThread();
    detachedThread.submit(new Runnable() {
      public void run() {
        testScope.setInternalStateForCurrentThread(internalState);
      }
    }).get();

    TestId actualTestId = detachedThread.submit(new GetProvidedTestId()).get();
    Assert.assertSame(expectedTestId, actualTestId);
  }

  /** Verify two tests sharing the same thread. */
  @Test
  public void testSharedThreadA() throws Exception {
    testSharedThread();
  }

  /** Verify two tests sharing the same thread. */
  @Test
  public void testSharedThreadB() throws Exception {
    testSharedThread();
  }

  /** Execute tests 1 and 2 sequentially in a predictable order. */
  void testSharedThread() throws Exception {
    synchronized (sharedThreadTestCounter) {
      int counter = sharedThreadTestCounter.incrementAndGet();
      if (counter == 1) {
        testSharedThreadTest1SameTestId();
      } else if (counter == 2) {
        testSharedThreadTest2NoTestId();
      } else {
        Assert.fail();
      }
    }
  }

  /** First test creating the thread shares the test scope. */
  void testSharedThreadTest1SameTestId() throws Exception {
    TestId actualTestId = sharedThread.submit(new GetProvidedTestId()).get();
    Assert.assertSame(expectedTestId, actualTestId);
  }

  /** Second test should not share scope with the first test. */
  void testSharedThreadTest2NoTestId() throws Exception {
    try {
      sharedThread.submit(new GetProvidedTestId()).get();
      Assert.fail("Test1 already finished and its test scope should be closed");
    } catch (ExecutionException e) {
      Assert.assertSame(ProvisionException.class, e.getCause().getClass());
      Assert.assertSame(IllegalStateException.class, e.getCause().getCause().getClass());
      Assert.assertTrue(e.getCause().getCause().getMessage().contains("GuiceBerry"));
    }

    final TestScope.InternalState internalState =
        testScope.getInternalStateForCurrentThread();
    sharedThread.submit(new Runnable() {
      public void run() {
        testScope.setInternalStateForCurrentThread(internalState);
      }
    }).get();

    TestId actualTestId = sharedThread.submit(new GetProvidedTestId()).get();
    Assert.assertSame(expectedTestId, actualTestId);
  }
}
