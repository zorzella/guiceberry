// Copyright 2008 Google Inc.  All Rights Reserved.
// Copyright 2009 Google Inc. All Rights Reserved.

package junit4.tutorial_1_server.prod;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) 
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER}) 
@BindingAnnotation
public @interface PortNumber {}