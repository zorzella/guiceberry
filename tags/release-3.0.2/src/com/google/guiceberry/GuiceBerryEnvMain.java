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

package com.google.guiceberry;

/**
 * If a GuiceBerry Env binds this interface, the implementing class's
 * {@link #run()} method will be called when the 
 * {@link com.google.inject.Injector} for that Env is first created, i.e. 
 * before running the first test that uses that Env -- or, more precisely,
 * even before the {@link TestWrapper#toRunBeforeTest()} method is invoked.
 * 
 * <p>Use this, for example, to start any servers you might need for your tests
 * in a given GuiceBerry Env.
 * 
 * <p>Note that the same thing could be accomplished through a
 * {@link TestWrapper}, but it's more verbose, less elegant and less correct.
 * 
 * <p>The name of the class alludes to the fact this is analogous to the section
 * of your production code's "main" method where, in a canonical Guice 
 * application, you start the server right after creating an 
 * {@link com.google.inject.Injector}.
 * 
 * @author Luiz-Otavio Zorzella
 */
public interface GuiceBerryEnvMain {

  /** @see GuiceBerryEnvMain */
  void run();
  
}
