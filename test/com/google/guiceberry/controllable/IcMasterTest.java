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

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.common.testing.TearDownStack;
import com.google.common.testing.junit3.TearDownTestCase;
import com.google.guiceberry.TestId;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import junit.framework.TestCase;

import java.lang.reflect.Constructor;

public class IcMasterTest extends TearDownTestCase {

  private static final TestId TEST_ID = buildTestId();

  private static TestId buildTestId() {
    try {
      Constructor<TestId> constructor = TestId.class.getDeclaredConstructor(String.class);
      constructor.setAccessible(true);
      return constructor.newInstance("foo");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
//    return new TestId("foo");
  }
  
  private enum MyEnum {
    ONE,
    TWO,
    THREE,
    FOUR
  }
  
  private static class MyGenericClass<T> {
    
    private final T tField;

    public MyGenericClass(T foo) {
      this.tField = foo;
    }
  }
  
  private static Module moduleForServerInjector () {
    return new AbstractModule(){
    
      @Override
      protected void configure() {
        bind(MyEnum.class).toInstance(MyEnum.ONE);
        bind(TestId.class).toInstance(TEST_ID);
      }

      @SuppressWarnings("unused")
      @Provides
      MyGenericClass<MyEnum> get() {
        return new MyGenericClass<MyEnum>(MyEnum.THREE);
      }
    };
  }
  
  private static final class ClassInServer {
    @Inject
    MyEnum myEnum;
    
    @Inject
    MyGenericClass<MyEnum> myGenericClassOfMyEnum;
    
  }
  
  private static final class MyTestCase extends TestCase {

    @Inject
    InjectionController<MyEnum> myEnumIc;
    
    @Inject
    InjectionController<MyGenericClass<MyEnum>> myGenericClassOfMyEnumIc;
  }
  
  /**
   * Just make sure square one is where we think it is
   */
  public void testNothingReally() throws Exception {
    Injector normalServerInjector = Guice.createInjector(moduleForServerInjector());
    assertEquals(MyEnum.ONE, normalServerInjector.getInstance(MyEnum.class));
  }
  
  public void testSimple() throws Exception {
    final IcMaster icMaster = new IcMaster()
      .thatControls(StaticMapInjectionController.strategy(), 
          Key.get(MyEnum.class),
          // TODO: awkward construct! At least document
          Key.get(new TypeLiteral<MyGenericClass<MyEnum>> (){}));

    Injector controlledServerInjector = 
      Guice.createInjector(icMaster.buildServerModule(moduleForServerInjector()));
    
    Injector testInjector = Guice.createInjector(buildTestModule(icMaster));
    
    final MyTestCase injected = testInjector.getInstance(MyTestCase.class);
    
    addTearDown(new TearDown() {
      
      public void tearDown() throws Exception {
        injected.myEnumIc.resetOverride();
      }
    });

    addTearDown(new TearDown() {
      
      public void tearDown() throws Exception {
        injected.myGenericClassOfMyEnumIc.resetOverride();
      }
    });
    
    ClassInServer instanceBefore = 
      controlledServerInjector.getInstance(ClassInServer.class);
    injected.myEnumIc.setOverride(MyEnum.TWO);
    injected.myGenericClassOfMyEnumIc.setOverride(new MyGenericClass<MyEnum>(MyEnum.FOUR));

    ClassInServer instanceAfter = 
      controlledServerInjector.getInstance(ClassInServer.class);
    
    assertEquals(MyEnum.ONE, instanceBefore.myEnum);
    assertEquals(MyEnum.TWO, instanceAfter.myEnum);

    assertEquals(MyEnum.THREE, instanceBefore.myGenericClassOfMyEnum.tField);
    assertEquals(MyEnum.FOUR, instanceAfter.myGenericClassOfMyEnum.tField);
  }

  private AbstractModule buildTestModule(final IcMaster icMaster) {
    return new AbstractModule() {
      @Override
      protected void configure() {
        install(icMaster.buildClientModule());
        bind(TestId.class).toInstance(TEST_ID);
        bind(TearDownAccepter.class).toInstance(new TearDownStack());
      }
    };
  }
  
  public void testIllegalArg() throws Exception {
    try {
      new IcMaster()
        .thatControls(StaticMapInjectionController.strategy(), 
            MyEnum.class, 
            MyEnum.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("The Key " +
          "'Key[type=com.google.guiceberry.controllable.IcMasterTest$MyEnum, annotation=[none]]'" +
          " has already been declared as controlled. " +
          "Remove the duplicate declaration.", 
          e.getMessage());
    }
  }

}
