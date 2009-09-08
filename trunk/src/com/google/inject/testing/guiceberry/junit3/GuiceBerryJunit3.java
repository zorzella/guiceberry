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

import com.google.common.collect.Maps;
import com.google.common.testing.TearDownAccepter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestId;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.TestScoped;

import junit.framework.TestCase;

import java.util.Map;

/**
 * Provides the tools to manage the JUnit tests that use {@code Guice}.  
 * <p> 
 * To use the {@link GuiceBerryJunit3} it is necessary to:
 * <ul><li>
 * Define a {@code Class<? extends Module>}  that specifies the appropriate bindings.
 * <li>
 * Annotate all the JUnit tests that use those bindings with {@link GuiceBerryEnv} 
 * annotation  and set the value of this annotation to the name of the module.
 * <li>
 * Call the {@link GuiceBerryJunit3#setUp(TestCase)} within the 
 * {@link TestCase#setUp()} method.
 * <li>
 * It is possible to define more than one module and different subclasses of 
 * {@link TestCase} may use the different modules (but don't have to).
 * </ul>
 * <p>
 *  GuiceBerry is thread-safe so tests can be run in parallel. 
 * 
 * @see Guice
 * 
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
 */
public class GuiceBerryJunit3 { 
  
  private static final GuiceBerryEnvRemapper DEFAULT_GUICE_BERRY_ENV_REMAPPER = 
	  new IdentityGuiceBerryEnvRemapper();

  static class GuiceBerryUniverse {

    Map<Class<? extends Module>, Injector> gbeClassToInjectorMap = Maps.newHashMap();
    
    InheritableThreadLocal<TestCase> testCurrentlyRunningOnThisThread  = 
      new InheritableThreadLocal<TestCase>();
  }

  static final GuiceBerryUniverse universe = new GuiceBerryUniverse();
  
  /**
   * Sets up the {@link TestCase} (given as the argument) to be ready to run. 
   * <p>
   * The {@link TestCase} given as the argument needs to be annotated with the 
   * {@link GuiceBerryEnv} annotation that provides the name of the 
   * {@code Class<? extends Module>} which specifies all the injected values 
   * used in this {@link TestCase}. It's necessary to add a binding that 
   * binds {@link TestScopeListener} to an instance of the class that
   * implements this interface. If there is no such a {@link TestScopeListener}
   * it is possible to bind it to an instance of {@link NoOpTestScopeListener}.
   * It is also necessary to install the {@link BasicJunit3Module}
   * 
   * <p>
   * Operations performed by this method include:
   * <ul>
   *    <li> If the {@link TestCase} is the instance of 
   *         {@link com.google.common.testing.junit3.TearDownTestCase},
   *         the test case's {@code tearDown()} method is guaranteed to be executed
   *         at the end of the test. 
   *         Otherwise  calling {@link GuiceBerryJunit3#tearDown(TestCase)} is programmer's 
   *         responsibility.
   *    <li> Gets the module from the class name provided by the 
   *         {@link GuiceBerryEnv} annotation  and injects the bindings specified by 
   *         the module into the {@link TestCase}. It also binds {@link TestId} 
   *         and {@link TestCase} to the corresponding {@link Provider} in the scope  
   *         defined by {@link TestScoped} annotation.
   *    <li> Notifies {@link TestScopeListener} that the test enters it's scope.
   *                
   *             
   * </ul>
   * <p>
   * It also starts storing the information that {@link TestCase} (given as the 
   * argument) is being run.
   * 
   * <p>
   * Note that you must call {@link #tearDown} if you call this method, unless
   * you're calling it from a subclass of {@link TearDownAccepter} in which case
   * it will be taken care of.
   * 
   * 
   * @param testCase The subclass of {@link TestCase} for which the 
   *     {@link GuiceBerryJunit3#setUp} is needed. 
   * @throws IllegalArgumentException If the {@link TestCase} provided  as 
   *     an argument  has no {@link GuiceBerryEnv} annotation or the module 
   *     provided by this annotation does not exist or {@link TestCase} is not 
   *     a type of {@code Class <? extends Module>}.
   * @throws RuntimeException If the previous test has not been finished 
   *     properly or it cannot create the instance of 
   *     {@code Class <? extends Module>} (For example: module doesn't have no 
   *     argument constructor). Also if there is a problem with injecting all 
   *     the bindings or the {@link TestScopeListener} isn't binded to anything.
   *                                            
   * @see TestScopeListener
   * @see TestId
   * @see TestScoped
   * @see GuiceBerryEnv                                      
   */
  public synchronized static void setUp(final TestCase testCase) { 
    GuiceBerryEnvRemapper remapper = getRemapper();
//    new GuiceBerryJunit3(testCase, 
//        getGbeAnnotation(testCase), 
//        remapper).goSetUp(testCase);
    new TestCaseScafolding(testCase, 
        getGbeAnnotation(testCase), 
        remapper, 
        universe).goSetUp(testCase);
  }
  
  
  /**   
   * You should only call this method if your test does <em>not</em> implement 
   * {@link TearDownAccepter}.
   *
   * <p>Stops storing the information that {@link TestCase} (given as the argument) 
   * is being run.
   * 
   * <p>Notifies {@link  TestScopeListener} corresponding to this module (
   * The {@link GuiceBerryEnv} annotation of the test case provides the name  
   * of the module)that {@link TestCase} has just become out of scope.
   * It also causes that {@link TestCase} is removed from the {@code TestScope} 
   * assigned to this  module. 
   * 
   * <p>Do not change the value (if any) of the
   * {@link GuiceBerryEnvRemapper#GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME} 
   * property, as it will likely cause the wrong {@link GuiceBerryEnv} to be
   * used on your tearDown.
   * 
   * <p>If your code, for whatever reason, happens to synchronize on the test
   * class while calling the {@link #setUp(TestCase)} method, <em>and</em> you 
   * run your tests in parallel, you should likewise synchronize the 
   * {@link #tearDown(TestCase)}.
   *
   * @throws IllegalArgumentException If the {@link TestCase} provided  as an 
   *     argument  has no {@link GuiceBerryEnv} annotation or the module 
   *     provided by this annotation does not exist or {@link TestCase} is not 
   *     a type of {@code Class <? extends Module>}.   
   * @throws RuntimeException If the method {@link GuiceBerryJunit3#setUp(TestCase)} 
   *     wasn't called before calling this method.                                 
   * 
   * @see TestScopeListener
   * @see JunitTestScope
   */
  public synchronized static void tearDown(TestCase testCase) {
    if (testCase instanceof TearDownAccepter) {
      throw new UnsupportedOperationException("You must not call " +
      		"GuiceBerryJunit3.tearDown (it's only needed for tests that do " +
      		"not implement TearDownAccepter).");
    }
    GuiceBerryEnvRemapper remapper = getRemapper();
    new TestCaseScafolding(
        testCase, 
        getGbeAnnotation(testCase),
        remapper,
        universe).goTearDown();
  }
  
  private static GuiceBerryEnv getGbeAnnotation(TestCase testCase) { 
    GuiceBerryEnv gbeAnnotation =
      testCase.getClass().getAnnotation(GuiceBerryEnv.class);
    return gbeAnnotation;
  }  
  
  @SuppressWarnings("unchecked")
  private static GuiceBerryEnvRemapper getRemapper() {
    String remapperName = System.getProperty(GuiceBerryEnvRemapper.GUICE_BERRY_ENV_REMAPPER_PROPERTY_NAME);
    if (remapperName != null) {
      Class<? extends GuiceBerryEnvRemapper> clazz;
      try {
        clazz = (Class<? extends GuiceBerryEnvRemapper>) 
        GuiceBerryJunit3.class.getClassLoader().loadClass(remapperName);
        } catch (ClassNotFoundException e) {
          throw new IllegalArgumentException(String.format(
            "Class '%s', which is being declared as a GuiceBerryEnvRemapper, does not exist.", remapperName), e);
        }
        if (!GuiceBerryEnvRemapper.class.isAssignableFrom(clazz)) {
          throw new IllegalArgumentException(String.format(
            "Class '%s' is being declared as a GuiceBerryEnvRemapper, but does not implement that interface", 
            remapperName));
        }
        try {
          return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
          throw new IllegalArgumentException(String.format(
            "GuiceBerryEnvRemapper '%s' must have public zero-arguments constructor", 
            remapperName), e);
        } catch (Exception e) {
          throw new RuntimeException(String.format(
            "There was a problem trying to instantiate your GuiceBerryEnvRemapper '%s'", remapperName), 
            e);
        }
    }
    return DEFAULT_GUICE_BERRY_ENV_REMAPPER;
  }

  static TestCase getActualTestCase() { 
     return universe.testCurrentlyRunningOnThisThread.get();
   }
  
  // METHODS BELOW ARE USED ONLY FOR TESTS  
  static void clear() {
    universe.gbeClassToInjectorMap = Maps.newHashMap();
    universe.testCurrentlyRunningOnThisThread.set(null);
  }
  
  static int numberOfInjectorsInUse(){
    return universe.gbeClassToInjectorMap.size();
  }
  
  static JunitTestScope getTestScopeForGbe(Class<?> key){
    Injector injector = universe.gbeClassToInjectorMap.get(key);
    if (injector == null) {
      return null;
    }
    return injector.getInstance(JunitTestScope.class);
  }
 
  /**
   * An "identity" remapper, that remaps a
   * {@link com.google.inject.testing.guiceberry.GuiceBerryEnv} to itself.
   * This remapper is installed by default.
   * 
   * {@inheritDoc}
   * 
   * @author Luiz-Otavio Zorzella
   */
  private static final class IdentityGuiceBerryEnvRemapper implements GuiceBerryEnvRemapper {
    public String remap(TestCase test, String env) {
      return env;
    }
  }
}