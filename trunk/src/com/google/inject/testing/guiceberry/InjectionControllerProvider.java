package com.google.inject.testing.guiceberry;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @deprecated this form of injection controller is no longer supported and will 
 * be killed in the future. Use the new 
 * {@link com.google.inject.testing.guiceberry.controllable} paradigm.
 */
@Deprecated
public class InjectionControllerProvider extends com.google.inject.testing.guiceberry.tokill.InjectionControllerProvider {

  @Inject
  public InjectionControllerProvider(
      Provider<TestId> testIdProvider){
    super(testIdProvider);
  }

}
