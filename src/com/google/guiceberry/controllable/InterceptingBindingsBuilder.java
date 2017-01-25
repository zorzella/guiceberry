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
package com.google.guiceberry.controllable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.internal.UniqueAnnotations;
import com.google.inject.name.Named;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.UntargettedBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Builds a {@link Module} that intercepts certain bindings.
 *
 * <p>The injector contains an extra binding for {@code Set<Key>} annotated
 * with the name "Interceptable". This bound set contains all intercepted keys.
 *
 * <h3>Limitations of the current implementation</h3>
 *
 * <p>All intercepted bindings must have binding targets - for example, a type
 * that is bound to itself cannot be intercepted:
 * <pre class="code">bind(MyServiceClass.class);</pre>
 *
 * <p>All intercepted bindings must be bound explicitly. Interception cannot
 * be applied to implicit bindings, or bindings that depend on
 * {@literal @}{@link ProvidedBy}, {@literal @}{@link ImplementedBy}
 * annotations.
 *
 * <p><strong>Implementation note:</strong> To intercept provision, an
 * additional, internal binding is created for each intercepted key. This is
 * used to bind the original (non-intercepted) provisioning strategy, and an
 * intercepting binding is created for the original key. This shouldn't have
 * any side-effects on the behaviour of the injector, but may confuse tools
 * that depend on {@link Injector#getBindings()} and similar methods.
 *
 * @author Jesse Wilson
 * @author Jerome Mourits
 * @author Luiz-Otavio Zorzella
 */
final class InterceptingBindingsBuilder {

  private static final Key<ProvisionInterceptor> INJECTION_INTERCEPTOR_KEY
      = Key.get(ProvisionInterceptor.class);

  private final Collection<Module> modules = new ArrayList<Module>();
  private final Set<Key<?>> keysToIntercept = Sets.newHashSet();
  private boolean tolerateUnmatchedInterceptions = false;

  public InterceptingBindingsBuilder() {
    // bind the keys to intercept
    modules.add(new AbstractModule() {
      @Override
      protected void configure() {}
      
      @SuppressWarnings("unused")
      @Provides @Named("Interceptable") Set<Key<?>> provideInterceptableKeys() {
        return Collections.<Key<?>>unmodifiableSet(keysToIntercept);
      }
    });
  }

  public InterceptingBindingsBuilder install(Module... modules) {
    this.modules.addAll(Arrays.asList(modules));
    return this;
  }

  public InterceptingBindingsBuilder install(Collection<? extends Module> modules) {
    this.modules.addAll(modules);
    return this;
  }

  public InterceptingBindingsBuilder intercept(Key<?>... keys) {
    this.keysToIntercept.addAll(Arrays.asList(keys));
    return this;
  }

  public InterceptingBindingsBuilder intercept(Collection<Key<?>> keys) {
    checkArgument(!keys.contains(INJECTION_INTERCEPTOR_KEY),
        "Cannot intercept the interceptor!");

    keysToIntercept.addAll(keys);
    return this;
  }

  public InterceptingBindingsBuilder intercept(Class<?>... classes) {
    List<Key<?>> keysAsList = new ArrayList<Key<?>>(classes.length);
    for (Class<?> clas : classes) {
      keysAsList.add(Key.get(clas));
    }

    return intercept(keysAsList);
  }

  public InterceptingBindingsBuilder tolerateUnmatchedInterceptions() {
    this.tolerateUnmatchedInterceptions = true;
    return this;
  }

  public Module build() {
    // record commands from the modules
    final List<Element> elements = Elements.getElements(modules);

    // rewrite the commands to insert interception
    return new Module() {
      public void configure(Binder binder) {
        ModuleRewriter rewriter = new ModuleRewriter(binder);
        rewriter.writeAll(elements);

        // fail if any interceptions were missing
        if (!tolerateUnmatchedInterceptions
            && !rewriter.keysIntercepted.equals(keysToIntercept)) {
          Set<Key<?>> keysNotIntercepted = Sets.newHashSet();
          keysNotIntercepted.addAll(keysToIntercept);
          keysNotIntercepted.removeAll(rewriter.keysIntercepted);
          binder.addError("An explicit binding is required for "
              + "all intercepted keys, but was not found for '%s'", keysNotIntercepted);
        }
      }
    };
  }

  /** Replays commands, inserting the InterceptingProvider where necessary. */
  private class ModuleRewriter extends ModuleWriter {
    private Set<Key<?>> keysIntercepted = Sets.newHashSet();
    
    public ModuleRewriter(Binder binder) {
      super(binder);
    }

    @Override
    public <T> Void visit(Binding<T> binding) {
      final Key<T> key = binding.getKey();

      if (!keysToIntercept.contains(key)) {
        return super.visit(binding);
      }

      binding.acceptTargetVisitor(new DefaultBindingTargetVisitor<T, Void>() {
        @Override
        public Void visit(UntargettedBinding<? extends T> untargettedBinding) {
          binder.addError("Cannot intercept bare binding of %s. " +
              		"You may only intercept bindings that bind a class to something.", key);
          return null;
        }
      });

      Key<T> anonymousKey = Key.get(key.getTypeLiteral(), UniqueAnnotations.create());
      binder.bind(key).toProvider(new InterceptingProvider<T>(key, binder.getProvider(anonymousKey)));

      ScopedBindingBuilder scopedBindingBuilder = bindKeyToTarget(binding, binder, anonymousKey);

      // we scope the user's provider, not the interceptor. This is dangerous,
      // but convenient. It means that although the user's provider will live
      // in its proper scope, the interceptor gets invoked without a scope
      applyScoping(binding, scopedBindingBuilder);

      keysIntercepted.add(key);
      return null;
    }
  }

  /**
   * Provide {@code T}, with a hook for an {@link ProvisionInterceptor}.
   */
  private static class InterceptingProvider<T> implements Provider<T> {
    private final Key<T> key;
    private Provider<ProvisionInterceptor> provisionInterceptorProvider;
    private Provider<? extends T> delegateProvider;

    public InterceptingProvider(Key<T> key, Provider<T> delegateProvider) {
      this.key = key;
      this.delegateProvider = delegateProvider;
    }

    @SuppressWarnings("unused")
    @Inject void initialize(Provider<ProvisionInterceptor> injectionInterceptorProvider) {
      this.provisionInterceptorProvider = injectionInterceptorProvider;
    }

    public T get() {
      checkNotNull(provisionInterceptorProvider, "injectionInterceptorProvider");
      return provisionInterceptorProvider.get().intercept(key, delegateProvider);
    }
  }
}
