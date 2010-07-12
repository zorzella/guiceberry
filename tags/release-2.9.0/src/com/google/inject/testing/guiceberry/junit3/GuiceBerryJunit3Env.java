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

import static com.google.inject.Scopes.SINGLETON;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;

/**
 * Merely a convenience class that you may choose to extend from when creating
 * your JUnit3 {@link GuiceBerryEnv}s. It does two things:
 * 
 * <ul>
 *  <li>It installs {@link BasicJunit3Module}
 *  <li>It requires a {@link #getTestScopeListener()} method to exist, and it
 *  binds this to {@link TestScopeListener} in the {@link Scopes#SINGLETON} 
 *  scope.
 * <ul>
 * 
 * While JUnit3 Envs are not required to extend from this class, they will
 * have to do the equivalent of these two operations. 
 * 
 * @author Luiz-Otavio Zorzella
 */
public abstract class GuiceBerryJunit3Env extends AbstractModule {

  protected Class<? extends TestScopeListener> getTestScopeListener() {
    return NoOpTestScopeListener.class;
  }

  @Override
  protected void configure() {
    install(new BasicJunit3Module());
    bind(TestScopeListener.class).to(getTestScopeListener()).in(SINGLETON);
  }
}
