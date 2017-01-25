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

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.UntargettedBinding;

import java.lang.annotation.Annotation;

/**
 * @author Jesse Wilson
 * @author Luiz-Otavio Zorzella
 */
//TODO: document and move to Guice?
class ModuleWriter extends DefaultElementVisitor<Void> {
  protected final Binder binder;

  ModuleWriter(Binder binder) {
    this.binder = binder;
  }

  @Override protected Void visitOther(Element element) {
    element.applyTo(binder);
    return null;
  }

  void writeAll(Iterable<? extends Element> elements) {
    for (Element element : elements) {
      element.acceptVisitor(this);
    }
  }
  
  /**
   * Execute this target against the linked binding builder.
   */
  <T> ScopedBindingBuilder bindKeyToTarget(
      final Binding<T> binding, final Binder binder, final Key<T> key) {
    return binding.acceptTargetVisitor(new DefaultBindingTargetVisitor<T, ScopedBindingBuilder>() {
      @Override
      public ScopedBindingBuilder visit(InstanceBinding<? extends T> binding) {
        binder.bind(key).toInstance(binding.getInstance());
        return null;
      }

      @Override
      public ScopedBindingBuilder visit(
          ProviderInstanceBinding<? extends T> binding) {
        return binder.bind(key).toProvider(binding.getProviderInstance());
      }

      @Override
      public ScopedBindingBuilder visit(ProviderKeyBinding<? extends T> binding) {
        return binder.bind(key).toProvider(binding.getProviderKey());
      }

      @Override
      public ScopedBindingBuilder visit(LinkedKeyBinding<? extends T> binding) {
        return binder.bind(key).to(binding.getLinkedKey());
      }

      @Override
      public ScopedBindingBuilder visit(UntargettedBinding<? extends T> binding) {
        return binder.bind(key);
      }
    });
  }
  
  protected void applyScoping(Binding<?> binding, final ScopedBindingBuilder scopedBindingBuilder) {
    binding.acceptScopingVisitor(new BindingScopingVisitor<Void>() {
      public Void visitEagerSingleton() {
        if (scopedBindingBuilder != null) {
          scopedBindingBuilder.asEagerSingleton();
        }
        return null;
      }

      public Void visitScope(Scope scope) {
        scopedBindingBuilder.in(scope);
        return null;
      }

      public Void visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
        scopedBindingBuilder.in(scopeAnnotation);
        return null;
      }

      public Void visitNoScoping() {
        // do nothing
        return null;
      }
    });
  }
}