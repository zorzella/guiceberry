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
package com.google.inject.testing.guiceberry.controllable;

import com.google.inject.Key;
import com.google.inject.internal.Objects;
import com.google.inject.testing.guiceberry.TestId;

/**
 * This is basically a pair of a controlled {@link Key} (i.e. a possible 
 * annotated {@link Class}) and a {@link TestId}. This is used as a 
 * {@link java.util.Map} key by the Controllable Injection framework to identify 
 * during runtime the instance that is being controlled.
 * 
 * @author Luiz-Otavio Zorzella
 *
 * @param <T> The type of {@link Key} in this class. 
 */
@Deprecated
public class ControllableId<T> {
  
  private final TestId testId;
  private final Key<T> key;

  public ControllableId(TestId test, Key<T> key) {
    this.testId = test;
    this.key = key;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(testId, key);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof ControllableId) {
      ControllableId<?> that = (ControllableId<?>) other;
      return Objects.equal(this.testId, that.testId)
        && Objects.equal(this.key, that.key);
    }
    return false;
  }
  
  @Override
  public String toString() {
    return String.format("['%s','%s']", testId.toString(), key.toString());
  }
}