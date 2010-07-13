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
package com.google.guiceberry.controllable;

import com.google.inject.Provider;
import com.google.inject.internal.Maps;
import com.google.guiceberry.controllable.IcStrategy;

import java.util.Map;

/**
 * The {@link #strategy()} static factory method gives forth the 
 * canonical {@link IcStrategy}.
 * 
 * <p>This strategy is quite capable, and should likely always be used, as long
 * as possible. The one and only constraint is that its internal {@link #map} 
 * static var needs to be shared between test and server Injector, i.e. running
 * in the same JVM and same (or child) ClassLoader. 
 * 
 * @author Luiz-Otavio Zorzella
 */
public final class SharedStaticVarIcStrategy {

  private static final Map<ControllableId<?>,Object> map = Maps.newHashMap();

  public static IcStrategy strategy() {
    return new IcStrategy(IcClientStrategyImpl.class, IcServerStrategyImpl.class);
  }

  @Deprecated
  public static IcStrategy buildStrategyCouple() {
    return strategy();
  }
  
  private static final class IcClientStrategyImpl implements IcStrategy.ClientSupport {
    public <T> void setOverride(ControllableId<T> pair, T override) {
      map.put(pair, override);
    }

    public <T> void resetOverride(ControllableId<T> controllableId) {
      map.remove(controllableId);
    }
  }
  
  private static final class IcServerStrategyImpl implements IcStrategy.ServerSupport {
    @SuppressWarnings("unchecked")
    public <T> T getOverride(
        ControllableId<T> controllableId, 
        Provider<? extends T> delegate) {
      if (!map.containsKey(controllableId)) {
        throw new IllegalArgumentException(String.format(
            "The injection of '%s' is not currently being controlled.", 
            controllableId.toString()));
      }
      return (T) map.get(controllableId);
    }

    public <T> boolean isControlled(ControllableId<T> controllableId) {
      return map.containsKey(controllableId);
    }
  }
}