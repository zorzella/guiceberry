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
package com.google.inject.testing.guiceberry.controllable;

import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * @deprecated this class will be made package-protected in the future, and 
 * should not be used externally.
 * 
 * <p>Intercepts object provision.
 *
 * @author Jesse Wilson
 * @author Jerome Mourits
 */
@Deprecated
public interface ProvisionInterceptor extends com.google.inject.commands.intercepting.ProvisionInterceptor {
  <T> T intercept(Key<T> key, Provider<? extends T> delegate);
}
