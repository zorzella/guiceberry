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

import java.lang.reflect.Type;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.util.Types;

/**
 * To implement controllable injections, what is fundamentally required is a way
 * for client (test) to send controllable-injection-related data to the server.
 * 
 * <p>Out-of-the-box, GuiceBerry offers a {@link StaticMapInjectionController}, 
 * which is ideal when you run both test and server in the same ClassLoader/JVM.
 * 
 * <p>When that is not possible, there are other ways to send this sort of data,
 * such as HTTP {@link javax.servlet.http.Cookie}s, {@link java.io.File}s in a 
 * shared disk, shared database tables and whatnot.
 * 
 * <p>This class makes the implementation of these strategies possible. Each
 * of these would simply require an implementation of the 
 * {@link IcStrategy.ClientSupport} and {@link IcStrategy.ServerSupport} 
 * interfaces.
 * 
 * <p>Note that each of these classes will be instantiated by a different 
 * {@link com.google.inject.Injector}.
 * 
 * <p>Also note that these implementations are not directly exposed to the 
 * end-user, but rather are a support for the Controllable Injection framework.
 * Note that the 
 * {@link IcStrategy.ClientSupport#setOverride(ControllableId, Object)} and
 * {@link IcStrategy.ClientSupport#resetOverride(ControllableId)} methods are
 * <em>indirectly</em> exposed to the end-user through 
 * {@link InjectionController#setOverride(Object)} and {@link InjectionController#resetOverride()}
 * respectively.
 * 
 * <p>Finally, note that some strategies might be fundamentally more "capable"
 * than others. E.g. a non-serializable class would pose a problem for a 
 * Cookie-based strategy, while the default (shared variable) strategy has no
 * difficulty in supporting it. In fact, the default strategy is fundamentally
 * the most capable strategy, and, thus, we say it's ideal.
 * 
 * @author Luiz-Otavio Zorzella
 */
public class IcStrategy {
  
  /**
   * The "client side" implementation of a Controllable Injection 
   * {@link IcStrategy}.
   */
  public interface ClientSupport {
    
    /**
     * Called every time a test controls an injection by calling 
     * {@link InjectionController#setOverride(Object)}. 
     * 
     * <p>From this moment on, a server injector's call to
     * {@link IcStrategy.ServerSupport#isControlled(ControllableId)} for this
     * {@code controllableId} should return true and a call to 
     * {@link IcStrategy.ServerSupport#getOverride(ControllableId, Provider)}
     * for this {@code controllableId} should return {@code override}.
     * 
     * <p>It is considered OK for a test to call 
     * {@link InjectionController#setOverride(Object)} multiple times, so this method
     * must also support it.
     */
    <T> void setOverride(ControllableId<T> controllableId, T override);
    
    /**
     * This method stops the controlling of an injection, i.e. it "undoes" 
     * <em>all</em> the previous calls to 
     * {@link #setOverride(ControllableId, Object)}.
     * 
     * <p>This method is called automatically by the framework when a test which 
     * has previously called {@link InjectionController#setOverride(Object)} is torn down,
     * but may also be called indirectly through {@link InjectionController#resetOverride()}.
     * 
     * <p>This method may be called multiple times in a row (i.e. you may be
     * asked to reset an override that is not currently being controlled), and
     * should support this.
     */
    <T> void resetOverride(ControllableId<T> controllableId);
  }
  
  /**
   * The "server side" implementation of a Controllable Injection 
   * {@link IcStrategy}.
   */
  public interface ServerSupport {
    
    /**
     * Returns true if the {@code controllableId} is currently being controlled.
     * 
     * <p>When this returns true, the framework will call 
     * {@link #getOverride(ControllableId, Provider)} to fulfill an injection, 
     * otherwise it will bypass it.
     */
    <T> boolean isControlled(ControllableId<T> controllableId);

    /**
     * Returns the current override for this Injection. The framework will 
     * never call this method unless {@link #isControlled(ControllableId)} 
     * returns true, so calling this method while not controlling an injection
     * is illegal and the outcome is unspecified (though it's conceivably 
     * possible to subvert the system with a race condition).
     * 
     * @param delegate what the server would return in absence of this injection 
     * being controlled. This param is expected to be ignored by most of the 
     * {@link IcStrategy}s, but might be useful for certain advanced purposes.
     */
    <T> T getOverride(ControllableId<T> controllableId, Provider<? extends T> delegate);
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
  
  Class<? extends IcStrategy.ClientSupport> clientSupportClass() {
    return clientSupportClass;
  }
  
  Class<? extends IcStrategy.ServerSupport> serverSupportClass() {
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