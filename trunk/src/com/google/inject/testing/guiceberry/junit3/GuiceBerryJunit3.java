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
import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.GuiceBerryEnvMain;
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

  /**
   * If something goes wrong trying to get a valid instance of an Injector
   * for some GuiceBerryEnv name, this instance is stored in the 
   * {@link #gbeClassToInjectorMap}, to allow for graceful error handling.
   */
  private static final Injector BOGUS_INJECTOR = Guice.createInjector();
  
  static Map<Class<? extends Module>, Injector> 
      gbeClassToInjectorMap = Maps.newHashMap();

  private static InheritableThreadLocal<TestCase> testCurrentlyRunningOnThisThread  = 
    new InheritableThreadLocal<TestCase>();

  private final TestCase testCase;
  private final GuiceBerryEnv gbeAnnotation;
  private final GuiceBerryEnvRemapper remapper;
  
  private GuiceBerryJunit3(
      TestCase testCase,
      GuiceBerryEnv gbeAnnotation,
      GuiceBerryEnvRemapper remapper) {
    this.testCase = testCase;
    this.gbeAnnotation = gbeAnnotation;
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
    new GuiceBerryJunit3(testCase, 
        getGbeAnnotation(testCase), 
        remapper).goSetUp(testCase);
  }
  
  private synchronized void goSetUp(final TestCase testCase) {
    if (gbeAnnotation == null) {
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
    //TODO(zorzella): kill this
    if (!Boolean.getBoolean("LENIENT_TEARDOWN")) {
      if (testCase instanceof TearDownAccepter) {
        throw new UnsupportedOperationException("You must not call " +
        		"GuiceBerryJunit3.tearDown (it's only needed for tests that do " +
        		"not implement TearDownAccepter).");
      }
    }
    GuiceBerryEnvRemapper remapper = getRemapper();
    new GuiceBerryJunit3(
        testCase, 
        getGbeAnnotation(testCase),
        remapper).goTearDown();
  }
  
  private void goTearDown() {
    Class<? extends Module> gbeClass = getGbeClassForTest();
    
    Injector injector = 
      gbeClassToInjectorMap.get(gbeClass);
    if (injector == BOGUS_INJECTOR) {
      // We failed to get a valid injector for this module in the setUp method,
      // so we just gracefully return, after cleaning up the threadlocal (which
      // normally would happen in the doTearDown method).
      testCurrentlyRunningOnThisThread.set(null);
      return;
    }
    
    notifyTestScopeListenerOfOutScope(injector);
    // TODO: this line used to be before the notifyTestScopeListenerOfOutScope
    // causing a bug -- e.d. a Provider<TestId> could not be used in the 
    // exitingScope method of the TestScopeListener. I haven't yet found a
    // good way to test this change.
    doTearDown(injector);
  }
  
  private void doSetUp() {
    final Class<? extends Module> gbeClass = 
      getGbeClassForTest();
    testCurrentlyRunningOnThisThread.set(testCase);
    Injector injector = getInjector(gbeClass);
    injector.getInstance(TestScopeListener.class).enteringScope();
    injectMembersIntoTest(gbeClass, injector); 
   
  }

  private void injectMembersIntoTest(
      final Class<? extends Module> moduleClass, Injector injector) {
  
    try {
      injector.injectMembers(testCase);
    } catch (RuntimeException e) {  
      String msg = String.format("Binding error in the module '%s': '%s'.", 
          moduleClass.toString(), e.getMessage());
      notifyTestScopeListenerOfOutScope(gbeClassToInjectorMap.get(moduleClass));
      throw new RuntimeException(msg, e);
    }
  }

  private Injector getInjector(final Class<? extends Module> gbeClass) {
    if (!gbeClassToInjectorMap.containsKey(gbeClass)) {    
      return foundGbeForTheFirstTime(gbeClass);  
    } else {
      Injector injector = 
        gbeClassToInjectorMap.get(gbeClass);
      if (injector == BOGUS_INJECTOR) {
        throw new RuntimeException(String.format(
            "Skipping '%s' GuiceBerryEnv which failed previously during injector creation.",
            gbeClass.getName()));
      }
      return injector; 
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
  private Class<? extends Module> getGbeClassForTest() {
  
    String gbeName = getGbeName(testCase);
    Class<? extends Module> gbeClass = 
      (Class<? extends Module>) getGbeClassFromClassName(gbeName);
    if (!Module.class.isAssignableFrom(gbeClass)) {
      String msg = String.format(
          "@%s class '%s' must be a Guice Module (i.e. implement com.google.inject.Module).", 
          GuiceBerryEnv.class.getSimpleName(),
          gbeClass.getName()); 
      throw new IllegalArgumentException(msg);
    }
    return gbeClass;
  }

  private static Class<?> getGbeClassFromClassName(
      String gbeName) {
    Class<?> className;
    try {
      className = GuiceBerryJunit3.class.getClassLoader().loadClass(gbeName);   
    } catch (ClassNotFoundException e) {  
      String msg = String.format(
    		  "@%s class '%s' was not found.",
    		  GuiceBerryEnv.class.getSimpleName(),
              gbeName.toString());
      throw new IllegalArgumentException(msg, e);
    }
    return className;
  }
  
  private static GuiceBerryEnv getGbeAnnotation(TestCase testCase) { 
    GuiceBerryEnv gbeAnnotation =
      testCase.getClass().getAnnotation(GuiceBerryEnv.class);
    return gbeAnnotation;
  }  
  
  private String getGbeName(TestCase testCase) {
    String declaredGbeName = this.gbeAnnotation.value();
    String result = remapper.remap(testCase, declaredGbeName);
    if (result == null) {
      throw new IllegalArgumentException(String.format(
          "The installed GuiceBerryEnvRemapper '%s' returned 'null' for the " +
          "'%s' test, which declares '%s' as its GuiceBerryEnv", 
          remapper.getClass().getName(), 
          testCase.getName(), declaredGbeName));
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
  private Injector foundGbeForTheFirstTime(
      final Class<? extends Module> gbeClass) {
    
    Injector injector = BOGUS_INJECTOR;
    
    try {
      Module gbeInstance = createGbeInstanceFromClass(gbeClass);
      injector = Guice.createInjector(gbeInstance);
      callGbeMainIfBound(injector);
      try {
        if (injector.getBindings().get(Key.get(TestScopeListener.class)) == null) {
          String msg = "TestScopeListener must be bound in your GuiceBerryEnv.";
          throw new RuntimeException(msg); 
        }
        // This is not actually used, but ensures at this point that the bound
        // TestScopeListener can be created
        @SuppressWarnings("unused")
        TestScopeListener testScopeListener = 
          injector.getInstance(TestScopeListener.class);
      } catch (ConfigurationException e) {
        String msg = String.format("Error while creating the instance of: " +
            "'%s': '%s'.", TestScopeListener.class, e.getMessage());
        throw new RuntimeException(msg, e); 
      }

      JunitTestScope testScope = injector.getInstance(JunitTestScope.class);
      return injector;
    } finally {
      // This is in the finally block to ensure that BOGUS_INJECTOR
      // is put in the map if things go bad.
      gbeClassToInjectorMap.put(gbeClass, injector);
    }
  }

  private void callGbeMainIfBound(Injector injector) {
    if (injector.getBindings().get(Key.get(GuiceBerryEnvMain.class)) != null) {
      injector.getInstance(GuiceBerryEnvMain.class).run();
    }
  }

  private Module createGbeInstanceFromClass(
      final Class<? extends Module> gbeClass) {
    Module result; 
    try {
      result = gbeClass.getConstructor().newInstance(); 
    } catch (NoSuchMethodException e) {
      String msg = String.format(
    		  "@%s class '%s' must have a public zero-arguments constructor",
    		  GuiceBerryEnv.class.getSimpleName(),
    		  gbeClass.getName()); 
      throw new IllegalArgumentException(msg, e); 
	} catch (Exception e) {
	      String msg = String.format(
	    		  "Error creating instance of @%s '%s'",
	    		  GuiceBerryEnv.class.getSimpleName(),
	    		  gbeClass.getName()); 
	        throw new IllegalArgumentException(msg, e); 
	}
    return result;
  }

  private static void notifyTestScopeListenerOfOutScope(Injector injector) {
    injector.getInstance(TestScopeListener.class).exitingScope();
  }

  private void doTearDown(Injector injector) {
  
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
    injector.getInstance(JunitTestScope.class).finishScope(testCase);    
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
  
  static TestCase getActualTestCase() { 
     return testCurrentlyRunningOnThisThread.get();
   }
  
  // METHODS BELOW ARE USED ONLY FOR TESTS  
  static void clear() {
    gbeClassToInjectorMap = Maps.newHashMap();
    testCurrentlyRunningOnThisThread.set(null);
  }
  
  static int numberOfInjectorsInUse(){
    return gbeClassToInjectorMap.size();
  }
  
  static JunitTestScope getTestScopeForGbe(Class<?> key){
    Injector injector = gbeClassToInjectorMap.get(key);
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