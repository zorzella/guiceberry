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

import junit.framework.TestCase;

/**
 * A remapper can be used to instruct GuiceBerry to use a different 
 * {@link com.google.inject.testing.guiceberry.GuiceBerryEnv} than the 
 * one actually specified in a given test class annotation.
 * 
 * <p>The primary purpose for this is to allow for running tests under
 * different "integration" modes, as can be seen in the tutorial example
 * TODO -- i.e. run any given test against multiple {@link GuiceBerryEnv}s.
 * 
 * @author Luiz-Otavio Zorzella
 */
public interface GuiceBerryEnvRemapper {

    /**
     * The name of the {@code System} property used to tell GuiceBerry 
     * which {@link GuiceBerryEnvRemapper} (if any) to use.
     */
    public static final String GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME = 
        "GuiceBerryEnvRemapper";

    /**
     * Returns the name of the {@link GuiceBerryEnv} to be used in place of 
     * the given {@code guiceBerryEnvName} for the given {@code testCase}.
     * 
     * <p>Note that the given {@code testCase} has not yet had its fields
     * {@code @Injected}, since GuiceBerry requires the result of this
     * method to choose the {@code Injector} to use.
     */
    String remap(TestCase testCase, String guiceBerryEnvName);
}
