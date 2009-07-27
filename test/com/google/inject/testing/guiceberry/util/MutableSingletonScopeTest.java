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

package com.google.inject.testing.guiceberry.util;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ScopeAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tests the {@link MutableSingletonScope} class.
 * 
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
 */
public class MutableSingletonScopeTest extends TearDownTestCase  {
  
  public void testBarServiceIsTheSameInstanceInMutableSingletonScope() {
    
    Something something = new Something();
    SomethingElse somethingElse = new SomethingElse();
    
    Injector injector = Guice.createInjector(new FooModule());
    assertNull(somethingElse.barService);
    assertNull(something.barService);
    injector.injectMembers(somethingElse);
    injector.injectMembers(something);
    assertSame(something.barService, somethingElse.barService);
  }
  
  public void testBarServiceIsDifferentInstanceAfterClearMutableSingletonScope() {

    Something something = new Something();
    SomethingElse somethingElse = new SomethingElse();
    
    Injector injector = Guice.createInjector(new FooModule());
    assertNull(something.barService);
    assertNull(something.barService);
    injector.injectMembers(somethingElse);
    injector.getInstance(MutableSingletonScope.class).clear();
    injector.injectMembers(something);
    assertNotSame(something.barService, somethingElse.barService);
  }
  
  private static class FooModule extends AbstractModule {
    @Override
    public void configure() {
      MutableSingletonScope mutableSingletonScope = new MutableSingletonScope();
      bind(MutableSingletonScope.class).toInstance(mutableSingletonScope);
      bindScope(MutableSingletonScoped.class, mutableSingletonScope);
      bind(BarService.class).in(MutableSingletonScoped.class);
    }
  }
  
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @ScopeAnnotation
  private @interface MutableSingletonScoped {}
     
  private static class BarService { }
  
  private static class Something {
    @Inject BarService barService;
  }
  
  private static class SomethingElse {
    @Inject BarService barService;
  }
}
