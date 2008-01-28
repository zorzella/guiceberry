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

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.testing.TearDown;
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

//move to JUnit package

/**
 * Provides the tools to manage the JUnit tests that use {@code Guice}.  
 * <p> 
 * To use the {@link GuiceBerryJunit} it is necessary to:
 * <ul><li>
 * Define a {@code Class<? extends Module>}  that specifies the appropriate bindings.
 * <li>
 * Annotate all the JUnit tests that use those bindings with {@link GuiceBerryEnv} 
 * annotation  and set the value of this annotation to the name of the module.
 * <li>
 * Call the {@link GuiceBerryJunit#setUp(TestCase)} within the 
 * {@link TestCase#setUp()} method.
 * <li>
 * It is possible to define more than one module and different subclasses of 
 * {@link TestCase} may use the different modules (but don't have to).
 * </ul>
 * <p>
 *  GooseBerry is thread-safe so tests can be run in parallel. 
 * 
 * @see Guice
 * 
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
 */
//TODO: rename to GuiceBerryJunit3?
public class GuiceBerryJunit { 
  
  /**
   * Singleton so that we can keep a persistent list of modules and 
   * internal structures corresponding to them. It lets store the information  
   * which test is currently running. It also allows to reuse an injector 
   * if there are multiple tests that need the same kind of injector. 
   */
  private static final GuiceBerryJunit instance = new GuiceBerryJunit();
  
  //TODO(zorzella): think about not needing the testScope and killing this
  private static final class GooseBerryStuff {
    
    private final Injector injector;
    private final JunitTestScope testScope;

    public GooseBerryStuff(Injector injector, JunitTestScope testScope) {
      this.injector = injector;
      this.testScope = testScope;
    }
  }
 
  private static Map<Class<? extends Module>, GooseBerryStuff> 
      moduleClassToGooseBerryStuffMap = Maps.newHashMap();

  private static InheritableThreadLocal<TestCase> testCurrentlyRunningOnThisThread  = 
    new InheritableThreadLocal<TestCase>();
 
  private GuiceBerryJunit(){}
  
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
   * It is also necessary to install the {@link BasicModule}
   * 
   * <p>
   * Operations performed by this method include:
   * <ul>
   *    <li> If the {@link TestCase} is the instance of 
   *         {@link com.google.common.testing.junit3.TearDownTestCase},
   *         the test case's {@code tearDown()} method is guaranteed to be executed
   *         at the end of the test. 
   *         Otherwise  calling {@link GuiceBerryJunit#tearDown(TestCase)} is programmer's 
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
   *     {@link GuiceBerryJunit#setUp} is needed. 
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
     
    GuiceBerryEnv gooseBerryModule = getGooseBerryModuleAnnotation(testCase);
    Objects.nonNull(gooseBerryModule, "GooseBerryModule annotation is null");   
    addGooseBerryTearDown(testCase);
    checkPreviousTestCalledTearDown(testCase);
    //Setup after registering tearDown so that if an exception is thrown here,
    //we still do a tearDown.
    instance.doSetUp(testCase);
    
  }
  
  /**   
   * Stops storing the information that {@link TestCase} (given as the argument) 
   * is being run.
   * <p>
   * 
   * Notifies {@link  TestScopeListener} corresponding to this module (
   * The {@link GuiceBerryEnv} annotation of the test case provides the name  
   * of the module)that {@link TestCase} has just become out of scope.
   * It also causes that {@link TestCase} is removed from the {@code TestScope} 
   * assigned to this  module. 
   *
   * @throws IllegalArgumentException If the {@link TestCase} provided  as an 
   *     argument  has no {@link GuiceBerryEnv} annotation or the module 
   *     provided by this annotation does not exist or {@link TestCase} is not 
   *     a type of {@code Class <? extends Module>}.   
   * @throws RuntimeException If the method {@link GuiceBerryJunit#setUp(TestCase)} 
   *     wasn't called before calling this method.                                 
   *                            
   *                                  
   * @see TestScopeListener
   * @see JunitTestScope
   */
  public synchronized static void tearDown(TestCase testCase) {
    Class<? extends Module> moduleClass = getModuleForTest(testCase);
    instance.doTearDown(testCase);
    notifyTestScopeListenerOfOutScope(moduleClass, testCase);
  }
  
  /**
   * Builds the name of the system property that controls which 
   * module is used to override the particular module. 
   * 
   * <p> If the value of the system property isn't set to anything or this 
   * system property  was cleared the module is not overridden. Otherwise the 
   * value of this system property is used to create the module (instead of the
   * value of {@link GuiceBerryEnv} annotation).       
   * 
   * To get the system property corresponding to the given module, call this 
   * method with the argument that is the name of this module.   
   *  
   * @param moduleClassName The module class name of the module for which system 
   *     property is needed.   
   * @return The name of the property corresponding to the module class name 
   *     from the parameter. 
   */
  public static String buildModuleOverrideProperty(String moduleClassName) {
    return "override"+GuiceBerryEnv.class.toString()+"-" + moduleClassName;
  }

  private void doSetUp(TestCase testCase) {   
      
    final Class<? extends Module> moduleClass = getModuleForTest(testCase);
    testCurrentlyRunningOnThisThread.set(testCase);
    Injector injector = getInjector(moduleClass);
    injector.getInstance(TestScopeListener.class).enteringScope();
    injectMembersIntoTest(testCase, moduleClass, injector); 
   
  }

  private void injectMembersIntoTest(TestCase testCase,
      final Class<? extends Module> moduleClass, Injector injector) {
  
    try {
      injector.injectMembers(testCase);
    } catch (RuntimeException e) {  
      String msg = String.format("Binding error in the module '%s': '%s'.", 
          moduleClass.toString(), e.getMessage());
      notifyTestScopeListenerOfOutScope(moduleClass, testCase);
      throw new RuntimeException(msg, e);
    }
  }

  private Injector getInjector(final Class<? extends Module> moduleClass) {
    if (!moduleClassToGooseBerryStuffMap.containsKey(moduleClass)) {    
      return foundModuleForTheFirstTime(moduleClass);  
    } else {
      return moduleClassToGooseBerryStuffMap.get(moduleClass).injector; 
    }
  }

  private static void checkPreviousTestCalledTearDown(TestCase testCase) {
    TestCase previousTestCase = testCurrentlyRunningOnThisThread.get();
    
    if (previousTestCase != null) {  
      String msg = String.format("Error while setting up a test: %s asked to " +
      		"set up test: %s.%s, but previous test:%s.%s did not properly " +
      		"call %s.tearDown().",
          GuiceBerryJunit.class.getCanonicalName(),
          testCase.getClass().getCanonicalName(),
          testCase.getName(), 
          previousTestCase.getClass().getCanonicalName(),
          previousTestCase.getName(),
          GuiceBerryJunit.class.getCanonicalName());
      throw new RuntimeException(msg);
    }
  }
  
  @SuppressWarnings("unchecked") 
  private static Class<? extends Module> getModuleForTest(TestCase testCase) {
  
    String gooseBerryModuleName = getGooseBerryModuleName(testCase);
    Class<? extends Module> moduleClass = 
      (Class<? extends Module>) getClassFromClassName(gooseBerryModuleName);
    if (!Module.class.isAssignableFrom(moduleClass)) {
      String msg = String.format(
          "Class '%s' must extend com.google.inject.Module", 
          moduleClass.toString()); 
      throw new IllegalArgumentException(msg);
    }
    return moduleClass;
  }

  private static Class<?> getClassFromClassName(
      String gooseBerryModuleName) {
    Class<?> className;
    try {
      className = Class.forName(gooseBerryModuleName);   
    } catch (ClassNotFoundException e) {  
      String msg = String.format("Required class '%s' not found.", 
          gooseBerryModuleName.toString());
      throw new IllegalArgumentException(msg, e);
    }
    return className;
  }
  
  private static GuiceBerryEnv getGooseBerryModuleAnnotation(TestCase testCase) { 
    GuiceBerryEnv gooseBerryModuleAnnotation =
      testCase.getClass().getAnnotation(GuiceBerryEnv.class);
    return gooseBerryModuleAnnotation;
  }  
  
  private static String getGooseBerryModuleName(TestCase testCase) {

    GuiceBerryEnv gooseBerryModuleAnnotation = getGooseBerryModuleAnnotation(testCase); 
    Objects.nonNull(gooseBerryModuleAnnotation, "GooseBerryModule annotation is null");
    String result = gooseBerryModuleAnnotation.value();
    String override = System.getProperty(buildModuleOverrideProperty(result));
    if (override != null) {
      return override;
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private Injector foundModuleForTheFirstTime(
      final Class<? extends Module> moduleClass) {
    
    Module userGooseBerryModule = createModuleFromModuleClass(moduleClass);
    Injector injector = Guice.createInjector(userGooseBerryModule);
     
    try {
      TestScopeListener testScopeListener = 
        injector.getInstance(TestScopeListener.class);
    //TODO(zorzella): catch ConfigurationException
    } catch (RuntimeException e) {
      String msg = String.format("Error while creating the instance of: " +
            "'%s': '%s'.", TestScopeListener.class, e.getMessage());
      throw new RuntimeException(msg, e); 
    }
    
    JunitTestScope testScope = injector.getInstance(JunitTestScope.class);
    GooseBerryStuff gooseBerryStuff = new GooseBerryStuff(injector, testScope);
    moduleClassToGooseBerryStuffMap.put(moduleClass, gooseBerryStuff);
    return injector;
  }

  private Module createModuleFromModuleClass(
      final Class<? extends Module> moduleClass) {
    Module testGooseBerryModule; 
    try {
      testGooseBerryModule = moduleClass.getConstructor().newInstance(); 
    } catch (Exception e) {  
      String msg = String.format("Error while creating the instance of: " +
      		"'%s': '%s'.", moduleClass.toString(), e.getMessage()); 
      throw new RuntimeException(msg, e); 
    }
    return testGooseBerryModule;
  }

  private static void notifyTestScopeListenerOfOutScope(
      Class <? extends Module> moduleClass,
      TestCase testCase) {
    Injector injector = 
      moduleClassToGooseBerryStuffMap.get(moduleClass).injector;
    injector.getInstance(TestScopeListener.class).exitingScope();
  }

  private void doTearDown(TestCase testCase) {
  
    if (testCurrentlyRunningOnThisThread.get() != testCase) {
      String msg = String.format( GuiceBerryJunit.class.toString() 
          + " cannot tear down "
          + testCase.toString()
          + " because that test never called "
          + GuiceBerryJunit.class.getCanonicalName()
          + ".setUp()"); 
      throw new RuntimeException(msg); 
    }
    testCurrentlyRunningOnThisThread.set(null);
    moduleClassToGooseBerryStuffMap.get(getModuleForTest(testCase)).testScope 
      .finishScope(testCase);    
  }  
  
  private static void addGooseBerryTearDown(final TestCase testCase) {
    if (testCase instanceof TearDownAccepter) {
      TearDownAccepter tdtc = (TearDownAccepter) testCase;
      tdtc.addRequiredTearDown(new TearDown() {
        public void tearDown() {
          GuiceBerryJunit.tearDown(testCase);
        }
      });
    }
  }
  
  static TestCase getActualTestCase(){ 
     return testCurrentlyRunningOnThisThread.get();
   }
  
//BELOW ARE CLASSES ARE USED ONLY FOR TESTS  
  static void clear() {
    moduleClassToGooseBerryStuffMap = Maps.newHashMap();
    testCurrentlyRunningOnThisThread.set(null);
  }
  
  static int numberOfInjectorsInUse(){
    return moduleClassToGooseBerryStuffMap.size();
  }
  
  static Injector getInjectorFromGB(Class<?> key){
    GooseBerryStuff gooseBerryStuff = moduleClassToGooseBerryStuffMap.get(key);
    if (gooseBerryStuff == null) {
      return null;
    }
    return gooseBerryStuff.injector;
  }
  
  static JunitTestScope getTestScopeFromGB(Class<?> key){
    GooseBerryStuff gooseBerryStuff = moduleClassToGooseBerryStuffMap.get(key);
    if (gooseBerryStuff == null) {
      return null;
    }
    return gooseBerryStuff.testScope;
  }
 
}