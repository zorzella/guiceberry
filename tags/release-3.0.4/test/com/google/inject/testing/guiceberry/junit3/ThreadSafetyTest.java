/*
 * Copyright (C) 2009 Google Inc.
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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.TestScoped;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@GuiceBerryEnv(
   "com.google.inject.testing.guiceberry.junit3" +
     ".ThreadSafetyTest" +
       "$Env")
public class ThreadSafetyTest extends TestCase {
  public static final int NTHREADS = 20;

  @Inject private Provider<Foo> fooProvider;
  @Inject private CyclicBarrier barrier;
  @Inject private AtomicInteger counter;
  
  /**
   * See <a href="http://code.google.com/p/guiceberry/issues/detail?id=11">
   * Bug 11</a>.
   */
  public void testStressTestScopeThreadSafety() throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(NTHREADS);
    final List<String> successes =
        Collections.synchronizedList(Lists.<String>newArrayList());
    final List<String> errors =
        Collections.synchronizedList(Lists.<String>newArrayList());
    counter.set(0);

    Callable<?> runnable = new Callable<Void>() {
      public Void call() throws BrokenBarrierException, InterruptedException {
        barrier.await();
        Foo foo1 = fooProvider.get();
        barrier.await();
        Foo foo2 = fooProvider.get();
        if (foo1 == foo2) {
          successes.add("Success");
        } else {
          errors.add("Error: " + foo1 + " != " + foo2);
        }
        return null;
      }
    };
    for (int i=0; i< NTHREADS; i++) {
      executorService.submit(runnable);
    }
    executorService.shutdown();
    assertTrue("Didn't finish in 10 seconds",
        executorService.awaitTermination(10, TimeUnit.SECONDS));
    assertTrue("Foo is " + fooProvider.get() +
        "; Errors: " + errors, errors.isEmpty());
  }

  public static final class Foo {
    private final int ordinal;
    @Inject
    public Foo(AtomicInteger counter) throws InterruptedException {
      ordinal = counter.incrementAndGet();
      // This sleep ensures that creation of Foo takes a non-trivial amount
      // of time -- time enough for, if there are concurrency issues with
      // creating it, for other threads to run and trigger a new instantiation
      Thread.sleep(100);
    }
    @Override
    public String toString() {
      return "Foo[" + ordinal + "]";
    }
  }

  public static class Env extends GuiceBerryJunit3Env {

    @Override
    public void configure() {
      super.configure();
      bind(Foo.class).in(TestScoped.class);
      bind(CyclicBarrier.class).toInstance(new CyclicBarrier(NTHREADS));
      bind(AtomicInteger.class).in(Singleton.class);
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    GuiceBerryJunit3.setUp(this);
  }
}