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

import java.lang.reflect.Type;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.util.Types;

//TODO:document One such class per injection controlling "strategy"
/**
 * 
 * @author Luiz-Otavio Zorzella
 */
public class IcStrategy {
  
  public interface ClientSupport {
    <T> void setOverride(ControllableId<T> controllableId, T override);
    <T> void resetOverride(ControllableId<T> controllableId);
  }
  
  public interface ServerSupport {
    <T> T getOverride(ControllableId<T> pair, Provider<? extends T> delegate);
  }
  
  private final Class<? extends IcStrategy.ClientSupport> clientSupportClass;
  private final Class<? extends IcStrategy.ServerSupport> serverSupportClass;

  public IcStrategy(
      Class<? extends IcStrategy.ClientSupport> icStrategyClientSupportClass,
      Class<? extends IcStrategy.ServerSupport> icStrategyServerSupportClass
      ) {
    this.clientSupportClass = icStrategyClientSupportClass;
    this.serverSupportClass = icStrategyServerSupportClass;
  }
  
  public Class<? extends IcStrategy.ClientSupport> clientSupportClass() {
    return clientSupportClass;
  }
  public Class<? extends IcStrategy.ServerSupport> serverSupportClass() {
    return serverSupportClass;
  }

  static Key<?> wrap(Type raw, Key<?> annotationHolder) {
    Type type = Types.newParameterizedType(
        raw, annotationHolder.getTypeLiteral().getType());
    if (annotationHolder.getAnnotation() != null) {
      return Key.get(type, annotationHolder.getAnnotation());
    } else if (annotationHolder.getAnnotationType() != null) {
      return Key.get(type, annotationHolder.getAnnotationType());
    } else {
      return Key.get(type);
    }
  }
}