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

import java.util.Map;

import junit.framework.TestCase;

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
  
  //TODO(zorzella): think about not needing the testScope and killing this
  private static final class GuiceBerryStuff {
    
    private final Injector injector;
    private final JunitTestScope testScope;

    public GuiceBerryStuff(Injector injector, JunitTestScope testScope) {
      this.injector = injector;
      this.testScope = testScope;
    }
  }
 
  private static Map<Class<? extends Module>, GuiceBerryStuff> 
      moduleClassToGuiceBerryStuffMap = Maps.newHashMap();

  private static InheritableThreadLocal<TestCase> testCurrentlyRunningOnThisThread  = 
    new InheritableThreadLocal<TestCase>();

  private final TestCase testCase;
  private final GuiceBerryEnvRemapper remapper;
 
  private GuiceBerryJunit3(TestCase testCase, GuiceBerryEnvRemapper remapper) {
    this.testCase = testCase;
    this.remapper = remapper;
  }
  
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
    new GuiceBerryJunit3(testCase, remapper).goSetUp(testCase);
  }
  
  private synchronized void goSetUp(final TestCase testCase) {
    GuiceBerryEnv guiceBerryEnvAnnotation = getGuiceBerryEnvAnnotation(testCase);
    if (guiceBerryEnvAnnotation == null) {
      throw new IllegalArgumentException(String.format(
    		  "Test class '%s' must have an @%s annotation.",
    		  testCase.getClass().getName(), GuiceBerryEnv.class.getSimpleName()));   
    }
    addGuiceBerryTearDown(testCase);
    checkPreviousTestCalledTearDown(testCase);
    //Setup after registering tearDown so that if an exception is thrown here,
    //we still do a tearDown.
    doSetUp();
    
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
    //TODO(zorzella): fix tests to enable this
    if (false) {
      if (testCase instanceof TearDownAccepter) {
        throw new UnsupportedOperationException("You must not call " +
        		"GuiceBerryJunit3.tearDown (it's only needed for tests that do " +
        		"not implement TearDownAccepter).");
      }
    }
    GuiceBerryEnvRemapper remapper = getRemapper();
    new GuiceBerryJunit3(testCase, remapper).goTearDown();
  }
  
  private void goTearDown() {
    Class<? extends Module> guiceBerryEnvClass = getGuiceBerryEnvClassForTest();
    notifyTestScopeListenerOfOutScope(guiceBerryEnvClass, testCase);
    // TODO: this line used to be before the notifyTestScopeListenerOfOutScope
    // causing a bug -- e.d. a Provider<TestId> could not be used in the 
    // exitingScope method of the TestScopeListener. I haven't yet found a
    // good way to test this change.
    doTearDown(guiceBerryEnvClass);
  }
  
  private void doSetUp() {
      
    final Class<? extends Module> guiceBerryEnvClass = 
      getGuiceBerryEnvClassForTest();
    testCurrentlyRunningOnThisThread.set(testCase);
    Injector injector = getInjector(guiceBerryEnvClass);
    injector.getInstance(TestScopeListener.class).enteringScope();
    injectMembersIntoTest(guiceBerryEnvClass, injector); 
   
  }

  private void injectMembersIntoTest(
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

  private Injector getInjector(final Class<? extends Module> guiceBerryEnvClass) {
    if (!moduleClassToGuiceBerryStuffMap.containsKey(guiceBerryEnvClass)) {    
      return foundModuleForTheFirstTime(guiceBerryEnvClass);  
    } else {
      return moduleClassToGuiceBerryStuffMap.get(guiceBerryEnvClass).injector; 
    }
  }

  private static void checkPreviousTestCalledTearDown(TestCase testCase) {
    TestCase previousTestCase = testCurrentlyRunningOnThisThread.get();
    
    if (previousTestCase != null) {  
      String msg = String.format("Error while setting up a test: %s asked to " +
      		"set up test: %s.%s, but previous test:%s.%s did not properly " +
      		"call %s.tearDown().",
          GuiceBerryJunit3.class.getCanonicalName(),
          testCase.getClass().getCanonicalName(),
          testCase.getName(), 
          previousTestCase.getClass().getCanonicalName(),
          previousTestCase.getName(),
          GuiceBerryJunit3.class.getCanonicalName());
      throw new RuntimeException(msg);
    }
  }
  
  @SuppressWarnings("unchecked") 
  private Class<? extends Module> getGuiceBerryEnvClassForTest() {
  
    String guiceBerryEnvName = getGuiceBerryEnvName(testCase);
    Class<? extends Module> moduleClass = 
      (Class<? extends Module>) getClassFromClassName(guiceBerryEnvName);
    if (!Module.class.isAssignableFrom(moduleClass)) {
      String msg = String.format(
          "@%s class '%s' must be a Guice Module (i.e. implement com.google.inject.Module).", 
          GuiceBerryEnv.class.getSimpleName(),
          moduleClass.getName()); 
      throw new IllegalArgumentException(msg);
    }
    return moduleClass;
  }

  private static Class<?> getClassFromClassName(
      String guiceBerryModuleName) {
    Class<?> className;
    try {
      className = GuiceBerryJunit3.class.getClassLoader().loadClass(guiceBerryModuleName);   
    } catch (ClassNotFoundException e) {  
      String msg = String.format(
    		  "@%s class '%s' was not found.",
    		  GuiceBerryEnv.class.getSimpleName(),
              guiceBerryModuleName.toString());
      throw new IllegalArgumentException(msg, e);
    }
    return className;
  }
  
  private static GuiceBerryEnv getGuiceBerryEnvAnnotation(TestCase testCase) { 
    GuiceBerryEnv guiceBerryEnvAnnotation =
      testCase.getClass().getAnnotation(GuiceBerryEnv.class);
    return guiceBerryEnvAnnotation;
  }  
  
  private String getGuiceBerryEnvName(TestCase testCase) {
    GuiceBerryEnv guiceBerryModuleAnnotation = getGuiceBerryEnvAnnotation(testCase); 
    String declaredGuiceBerryEnv = guiceBerryModuleAnnotation.value();
    String result = remapper.remap(testCase, declaredGuiceBerryEnv);
    if (result == null) {
      throw new IllegalArgumentException(String.format(
          "The installed GuiceBerryEnvRemapper '%s' returned 'null' for the " +
          "'%s' test, which declares '%s' as its GuiceBerryEnv", 
          remapper.getClass().getName(), 
          testCase.getName(), declaredGuiceBerryEnv));
    }
    return result;
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

  @SuppressWarnings("unchecked")
  private Injector foundModuleForTheFirstTime(
      final Class<? extends Module> guiceBerryEnvClass) {
    
    Module guiceBerryEnvInstance = createGuiceBerryInstanceFromClass(guiceBerryEnvClass);
    Injector injector = Guice.createInjector(guiceBerryEnvInstance);
     
    try {
    // This is not actually used, but ensures at this point that 
    // TestScopeListener has been bound 
    @SuppressWarnings("unused")
	TestScopeListener testScopeListener = 
        injector.getInstance(TestScopeListener.class);
    //TODO(zorzella): catch ConfigurationException
    } catch (RuntimeException e) {
      String msg = String.format("Error while creating the instance of: " +
            "'%s': '%s'.", TestScopeListener.class, e.getMessage());
      throw new RuntimeException(msg, e); 
    }
    
    JunitTestScope testScope = injector.getInstance(JunitTestScope.class);
    GuiceBerryStuff guiceBerryStuff = new GuiceBerryStuff(injector, testScope);
    moduleClassToGuiceBerryStuffMap.put(guiceBerryEnvClass, guiceBerryStuff);
    return injector;
  }

  private Module createGuiceBerryInstanceFromClass(
      final Class<? extends Module> guiceBerryEnvClass) {
    Module result; 
    try {
      result = guiceBerryEnvClass.getConstructor().newInstance(); 
    } catch (NoSuchMethodException e) {
      String msg = String.format(
    		  "@%s class '%s' must have a public zero-arguments constructor",
    		  GuiceBerryEnv.class.getSimpleName(),
    		  guiceBerryEnvClass.getName()); 
      throw new IllegalArgumentException(msg, e); 
	} catch (Exception e) {
	      String msg = String.format(
	    		  "Error creating instance of @%s '%s'",
	    		  GuiceBerryEnv.class.getSimpleName(),
	    		  guiceBerryEnvClass.getName()); 
	        throw new IllegalArgumentException(msg, e); 
	}
    return result;
  }

  private static void notifyTestScopeListenerOfOutScope(
      Class <? extends Module> moduleClass,
      TestCase testCase) {
    Injector injector = 
      moduleClassToGuiceBerryStuffMap.get(moduleClass).injector;
    injector.getInstance(TestScopeListener.class).exitingScope();
  }

  private void doTearDown(Class<? extends Module> guiceBerryEnvClass) {
  
    if (testCurrentlyRunningOnThisThread.get() != testCase) {
      String msg = String.format( GuiceBerryJunit3.class.toString() 
          + " cannot tear down "
          + testCase.toString()
          + " because that test never called "
          + GuiceBerryJunit3.class.getCanonicalName()
          + ".setUp()"); 
      throw new RuntimeException(msg); 
    }
    testCurrentlyRunningOnThisThread.set(null);
    moduleClassToGuiceBerryStuffMap.get(guiceBerryEnvClass).testScope 
      .finishScope(testCase);    
  }  
  
  private void addGuiceBerryTearDown(final TestCase testCase) {
    if (testCase instanceof TearDownAccepter) {
      TearDownAccepter tdtc = (TearDownAccepter) testCase;
      tdtc.addTearDown(new TearDown() {
        public void tearDown() {
          goTearDown();
        }
      });
    }
  }
  
  static TestCase getActualTestCase(){ 
     return testCurrentlyRunningOnThisThread.get();
   }
  
//BELOW ARE CLASSES ARE USED ONLY FOR TESTS  
  static void clear() {
    moduleClassToGuiceBerryStuffMap = Maps.newHashMap();
    testCurrentlyRunningOnThisThread.set(null);
  }
  
  static int numberOfInjectorsInUse(){
    return moduleClassToGuiceBerryStuffMap.size();
  }
  
  static Injector getInjectorFromGB(Class<?> key){
    GuiceBerryStuff guiceBerryStuff = moduleClassToGuiceBerryStuffMap.get(key);
    if (guiceBerryStuff == null) {
      return null;
    }
    return guiceBerryStuff.injector;
  }
  
  static JunitTestScope getTestScopeFromGB(Class<?> key){
    GuiceBerryStuff guiceBerryStuff = moduleClassToGuiceBerryStuffMap.get(key);
    if (guiceBerryStuff == null) {
      return null;
    }
    return guiceBerryStuff.testScope;
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
  private static class IdentityGuiceBerryEnvRemapper implements GuiceBerryEnvRemapper {
    public String remap(TestCase test, String env) {
      return env;
    }
  }
}