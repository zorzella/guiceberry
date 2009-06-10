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

import java.util.Map;

import com.google.inject.Provider;
import com.google.inject.internal.Maps;
import com.google.inject.testing.guiceberry.controllable.IcStrategyCouple.IcClientStrategy;
import com.google.inject.testing.guiceberry.controllable.IcStrategyCouple.IcServerStrategy;

//TODO: document
/**
 * @author Luiz-Otavio Zorzella
 */
public final class SharedStaticVarIcStrategy {

  private static final Map<ControllableId<?>,Object> map = Maps.newHashMap();

  public IcStrategyCouple getControllerSupport() {
    return new IcStrategyCouple(MyClientController.class, MyServerController.class);
  }
  
  private static final class MyClientController implements IcClientStrategy {
    public <T> void setOverride(ControllableId<T> pair, T override) {
      map.put(pair, override);
    }
  }
  
  private static final class MyServerController implements IcServerStrategy {
    @SuppressWarnings("unchecked")
    public <T> T getOverride(ControllableId<T> pair, Provider<? extends T> delegate) {
      if (!map.containsKey(pair)) {
        return delegate.get();
      }
      return (T) map.get(pair);
    }
  }
}