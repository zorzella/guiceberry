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

import com.google.inject.Module;

/**
 * GuiceBerry uses this interface to allow for arbitrarily associating a test
 * with a GuiceBerry Env.
 *
 * <p>A "GuiceBerry Env" is a defined as a class that:
 * 
 * <ul>
 *   <ul>is a Guice {@link Module}
 *   <ul>completely defines an {@link com.google.inject.Injector} (i.e. there
 *     are no "missing" bindings. In other words, a call to
 *     {@code Guice.createInjector(anyGuiceBerryEnv)} has to succeed.
 *   <ul>installs the {@link GuiceBerryModule} bindings.
 *   <ul>GuiceBerry will use to create an Injector to injects a test class.
 * </ul>
 *
 * @author Luiz-Otavio "Z" Zorzella
 */
public interface GuiceBerryEnvChooser {

  /**
   * Returns the Class of the GuiceBerry Env to use.
   */
  Class<? extends Module> guiceBerryEnvToUse();
}
