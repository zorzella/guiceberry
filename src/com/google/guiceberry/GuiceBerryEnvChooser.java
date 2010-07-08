// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.guiceberry;

/**
 * @author zorzella@google.com (Your Name Here)
 */
public @interface GuiceBerryEnvChooser {

  Class<? extends EnvChooser> value();

}
