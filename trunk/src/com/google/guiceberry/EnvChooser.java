// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.guiceberry;

import com.google.inject.Module;

/**
 * GuiceBerry uses this interface to allow for arbitrarily associating a test
 * with a GuiceBerry Environment -- i.e. a {@link Module} that defines an 
 * {@link com.google.inject.Injector}, which is used to inject a test class.
 * 
 * @author Luiz-Otavio "Z" Zorzella
 */
public interface EnvChooser {

  /**
   * 
   * @param testDescription
   * @return
   */
  Class<? extends Module> guiceBerryEnvToUse(TestDescription testDescription);
}
