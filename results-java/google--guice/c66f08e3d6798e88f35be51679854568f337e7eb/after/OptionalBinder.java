/**
 * Copyright (C) 2014 Google Inc.
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

package com.google.inject.multibindings;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.inject.multibindings.Multibinder.checkConfiguration;
import static com.google.inject.util.Types.newParameterizedType;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.Element;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderLookup;
import com.google.inject.spi.ProviderWithDependencies;
import com.google.inject.spi.ProviderWithExtensionVisitor;
import com.google.inject.spi.Toolable;
import com.google.inject.util.Types;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.reflect.Type;
import java.util.Set;

import javax.inject.Qualifier;


/**
 * An API to bind optional values, optionally with a default value.
 * OptionalBinder fulfills two roles: <ol>
 * <li>It allows a framework to define an injection point that may or
 *     may not be bound by users.
 * <li>It allows a framework to supply a default value that can be changed
 *     by users.
 * </ol>
 *
 * <p>When an OptionalBinder is added, it will always supply the bindings:
 * {@code Optional<T>} and {@code Optional<Provider<T>>}.  If
 * {@link #setBinding} or {@link #setDefault} are called, it will also
 * bind {@code T}.
 *
 * <p>{@code setDefault} is intended for use by frameworks that need a default
 * value.  User code can call {@code setBinding} to override the default.
 * <b>Warning: Even if setBinding is called, the default binding
 * will still exist in the object graph.  If it is a singleton, it will be
 * instantiated in {@code Stage.PRODUCTION}.</b>
 *
 * <p>If setDefault or setBinding are linked to Providers, the Provider may return
 * {@code null}.  If it does, the Optional bindings will be absent.  Binding
 * setBinding to a Provider that returns null will not cause OptionalBinder
 * to fall back to the setDefault binding.
 *
 * <p>If neither setDefault nor setBinding are called, the optionals will be
 * absent.  Otherwise, the optionals will return present if they are bound
 * to a non-null value.
 *
 * <p>Values are resolved at injection time. If a value is bound to a
 * provider, that provider's get method will be called each time the optional
 * is injected (unless the binding is also scoped, or an optional of provider is
 * injected).
 *
 * <p>Annotations are used to create different optionals of the same key/value
 * type. Each distinct annotation gets its own independent binding.
 *
 * <pre><code>
 * public class FrameworkModule extends AbstractModule {
 *   protected void configure() {
 *     OptionalBinder.newOptionalBinder(binder(), Renamer.class);
 *   }
 * }</code></pre>
 *
 * <p>With this module, an {@link Optional}{@code <Renamer>} can now be
 * injected.  With no other bindings, the optional will be absent.  However,
 * once a user adds a binding:
 *
 * <pre><code>
 * public class UserRenamerModule extends AbstractModule {
 *   protected void configure() {
 *     OptionalBinder.newOptionalBinder(binder(), Renamer.class)
 *         .setBinding().to(ReplacingRenamer.class);
 *   }
 * }</code></pre>
 * .. then the {@code Optional<Renamer>} will be present and supply the
 * ReplacingRenamer.
 *
 * <p>Default values can be supplied using:
 * <pre><code>
 * public class FrameworkModule extends AbstractModule {
 *   protected void configure() {
 *     OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, LookupUrl.class))
 *         .setDefault().to(DEFAULT_LOOKUP_URL);
 *   }
 * }</code></pre>
 * With the above module, code can inject an {@code @LookupUrl String} and it
 * will supply the DEFAULT_LOOKUP_URL.  A user can change this value by binding
 * <pre><code>
 * public class UserLookupModule extends AbstractModule {
 *   protected void configure() {
 *     OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, LookupUrl.class))
 *         .setBinding().to(CUSTOM_LOOKUP_URL);
 *   }
 * }</code></pre>
 * ... which will override the default value.
 *
 * @author sameb@google.com (Sam Berlin)
 */
public abstract class OptionalBinder<T> {
  private OptionalBinder() {}

  public static <T> OptionalBinder<T> newOptionalBinder(Binder binder, Class<T> type) {
    return newOptionalBinder(binder, Key.get(type));
  }

  public static <T> OptionalBinder<T> newOptionalBinder(Binder binder, TypeLiteral<T> type) {
    return newOptionalBinder(binder, Key.get(type));
  }

  public static <T> OptionalBinder<T> newOptionalBinder(Binder binder, Key<T> type) {
    binder = binder.skipSources(OptionalBinder.class, RealOptionalBinder.class);
    RealOptionalBinder<T> optionalBinder = new RealOptionalBinder<T>(binder, type);
    binder.install(optionalBinder);
    return optionalBinder;
  }

  @SuppressWarnings("unchecked")
  static <T> TypeLiteral<Optional<T>> optionalOf(
      TypeLiteral<T> type) {
    return (TypeLiteral<Optional<T>>) TypeLiteral.get(
        Types.newParameterizedType(Optional.class,  type.getType()));
  }

  @SuppressWarnings("unchecked")
  static <T> TypeLiteral<Optional<javax.inject.Provider<T>>> optionalOfJavaxProvider(
      TypeLiteral<T> type) {
    return (TypeLiteral<Optional<javax.inject.Provider<T>>>) TypeLiteral.get(
        Types.newParameterizedType(Optional.class,
            newParameterizedType(javax.inject.Provider.class, type.getType())));
  }

  @SuppressWarnings("unchecked")
  static <T> TypeLiteral<Optional<Provider<T>>> optionalOfProvider(TypeLiteral<T> type) {
    return (TypeLiteral<Optional<Provider<T>>>) TypeLiteral.get(Types.newParameterizedType(
        Optional.class, newParameterizedType(Provider.class, type.getType())));
  }

  @SuppressWarnings("unchecked")
  static <T> Key<Provider<T>> providerOf(Key<T> key) {
    Type providerT = Types.providerOf(key.getTypeLiteral().getType());
    return (Key<Provider<T>>) key.ofType(providerT);
  }

  /**
   * Returns a binding builder used to set the default value that will be injected.
   * The binding set by this method will be ignored if {@link #setBinding} is called.
   *
   * <p>It is an error to call this method without also calling one of the {@code to}
   * methods on the returned binding builder.
   */
  public abstract LinkedBindingBuilder<T> setDefault();


  /**
   * Returns a binding builder used to set the actual value that will be injected.
   * This overrides any binding set by {@link #setDefault}.
   *
   * <p>It is an error to call this method without also calling one of the {@code to}
   * methods on the returned binding builder.
   */
  public abstract LinkedBindingBuilder<T> setBinding();

  enum Source { DEFAULT, ACTUAL }

  @Retention(RUNTIME)
  @Qualifier
  @interface Default {
    String value();
  }

  @Retention(RUNTIME)
  @Qualifier
  @interface Actual {
    String value();
  }

  /**
   * The actual OptionalBinder plays several roles.  It implements Module to hide that
   * fact from the public API, and installs the various bindings that are exposed to the user.
   */
  static final class RealOptionalBinder<T> extends OptionalBinder<T> implements Module {
    private final Key<T> typeKey;
    private final Key<Optional<T>> optionalKey;
    private final Key<Optional<javax.inject.Provider<T>>> optionalJavaxProviderKey;
    private final Key<Optional<Provider<T>>> optionalProviderKey;
    private final Provider<Optional<Provider<T>>> optionalProviderT;
    private final Key<T> defaultKey;
    private final Key<T> actualKey;

    /** the target injector's binder. non-null until initialization, null afterwards */
    private Binder binder;
    /** the default binding, for the SPI. */
    private Binding<T> defaultBinding;
    /** the actual binding, for the SPI */
    private Binding<T> actualBinding;

    /** the dependencies -- initialized with defaults & overridden when tooled. */
    private Set<Dependency<?>> dependencies;
    /** the dependencies -- initialized with defaults & overridden when tooled. */
    private Set<Dependency<?>> providerDependencies;

    private RealOptionalBinder(Binder binder, Key<T> typeKey) {
      this.binder = binder;
      this.typeKey = checkNotNull(typeKey);
      TypeLiteral<T> literal = typeKey.getTypeLiteral();
      this.optionalKey = typeKey.ofType(optionalOf(literal));
      this.optionalJavaxProviderKey = typeKey.ofType(optionalOfJavaxProvider(literal));
      this.optionalProviderKey = typeKey.ofType(optionalOfProvider(literal));
      this.optionalProviderT = binder.getProvider(optionalProviderKey);
      String name = RealElement.nameOf(typeKey);
      this.defaultKey = Key.get(typeKey.getTypeLiteral(), new DefaultImpl(name));
      this.actualKey = Key.get(typeKey.getTypeLiteral(), new ActualImpl(name));
      // Until the injector initializes us, we don't know what our dependencies are,
      // so initialize to the whole Injector (like Multibinder, and MapBinder indirectly).
      this.dependencies = ImmutableSet.<Dependency<?>>of(Dependency.get(Key.get(Injector.class)));
      this.providerDependencies =
          ImmutableSet.<Dependency<?>>of(Dependency.get(Key.get(Injector.class)));
    }

    /**
     * Adds a binding for T. Multiple calls to this are safe, and will be collapsed as duplicate
     * bindings.
     */
    private void addDirectTypeBinding(Binder binder) {
      binder.bind(typeKey).toProvider(new RealOptionalBinderProviderWithDependencies<T>(typeKey) {
        public T get() {
          Optional<Provider<T>> optional = optionalProviderT.get();
          if (optional.isPresent()) {
            return optional.get().get();
          }
          // Let Guice handle blowing up if the injection point doesn't have @Nullable
          // (If it does have @Nullable, that's fine.  This would only happen if
          //  setBinding/setDefault themselves were bound to 'null').
          return null;
        }

        public Set<Dependency<?>> getDependencies() {
          return dependencies;
        }
      });
    }

    @Override public LinkedBindingBuilder<T> setDefault() {
      checkConfiguration(!isInitialized(), "already initialized");
      addDirectTypeBinding(binder);
      return binder.bind(defaultKey);
    }

    @Override public LinkedBindingBuilder<T> setBinding() {
      checkConfiguration(!isInitialized(), "already initialized");
      addDirectTypeBinding(binder);
      return binder.bind(actualKey);
    }

    public void configure(Binder binder) {
      checkConfiguration(!isInitialized(), "OptionalBinder was already initialized");

      binder.bind(optionalProviderKey).toProvider(
          new RealOptionalBinderProviderWithDependencies<Optional<Provider<T>>>(typeKey) {
        private Optional<Provider<T>> optional;

        @Toolable @Inject void initialize(Injector injector) {
          RealOptionalBinder.this.binder = null;
          actualBinding = injector.getExistingBinding(actualKey);
          defaultBinding = injector.getExistingBinding(defaultKey);
          Binding<T> binding = null;
          if (actualBinding != null) {
            // TODO(sameb): Consider exposing an option that will allow
            // ACTUAL to fallback to DEFAULT if ACTUAL's provider returns null.
            // Right now, an ACTUAL binding can convert from present -> absent
            // if it's bound to a provider that returns null.
            binding = actualBinding;
          } else if (defaultBinding != null) {
            binding = defaultBinding;
          }

          if (binding != null) {
            optional = Optional.of(binding.getProvider());
            RealOptionalBinder.this.dependencies =
                ImmutableSet.<Dependency<?>>of(Dependency.get(binding.getKey()));
            RealOptionalBinder.this.providerDependencies =
                ImmutableSet.<Dependency<?>>of(Dependency.get(providerOf(binding.getKey())));
          } else {
            optional = Optional.absent();
            RealOptionalBinder.this.dependencies = ImmutableSet.of();
            RealOptionalBinder.this.providerDependencies = ImmutableSet.of();
          }
        }

        public Optional<Provider<T>> get() {
          return optional;
        }

        public Set<Dependency<?>> getDependencies() {
          return providerDependencies;
        }
      });

      // Optional is immutable, so it's safe to expose Optional<Provider<T>> as
      // Optional<javax.inject.Provider<T>> (since Guice provider implements javax Provider).
      @SuppressWarnings({"unchecked", "cast"})
      Key massagedOptionalProviderKey = (Key) optionalProviderKey;
      binder.bind(optionalJavaxProviderKey).to(massagedOptionalProviderKey);

      binder.bind(optionalKey).toProvider(new RealOptionalKeyProvider());
    }

    private class RealOptionalKeyProvider
        extends RealOptionalBinderProviderWithDependencies<Optional<T>>
        implements ProviderWithExtensionVisitor<Optional<T>>,
            OptionalBinderBinding<Optional<T>>,
            Provider<Optional<T>> {
      RealOptionalKeyProvider() {
        super(typeKey);
      }

      public Optional<T> get() {
        Optional<Provider<T>> optional = optionalProviderT.get();
        if (optional.isPresent()) {
          return Optional.fromNullable(optional.get().get());
        } else {
          return Optional.absent();
        }
      }

      public Set<Dependency<?>> getDependencies() {
        return dependencies;
      }

      @SuppressWarnings("unchecked")
      public <B, R> R acceptExtensionVisitor(BindingTargetVisitor<B, R> visitor,
          ProviderInstanceBinding<? extends B> binding) {
        if (visitor instanceof MultibindingsTargetVisitor) {
          return ((MultibindingsTargetVisitor<Optional<T>, R>) visitor).visit(this);
        } else {
          return visitor.visit(binding);
        }
      }

      public Key<Optional<T>> getKey() {
        return optionalKey;
      }

      public Binding<?> getActualBinding() {
        if (isInitialized()) {
          return actualBinding;
        } else {
          throw new UnsupportedOperationException(
              "getActualBinding() not supported from Elements.getElements, requires an Injector.");
        }
      }

      public Binding<?> getDefaultBinding() {
        if (isInitialized()) {
          return defaultBinding;
        } else {
          throw new UnsupportedOperationException(
              "getDefaultBinding() not supported from Elements.getElements, requires an Injector.");
        }
      }

      public boolean containsElement(Element element) {
        Key<?> elementKey;
        if (element instanceof Binding) {
          elementKey = ((Binding<?>) element).getKey();
        } else if (element instanceof ProviderLookup) {
          elementKey = ((ProviderLookup<?>) element).getKey();
        } else {
          return false; // cannot match;
        }

        return elementKey.equals(optionalKey)
            || elementKey.equals(optionalProviderKey)
            || elementKey.equals(optionalJavaxProviderKey)
            || elementKey.equals(defaultKey)
            || elementKey.equals(actualKey)
            || matchesTypeKey(element, elementKey);
      }
    }

    /** Returns true if the key & element indicate they were bound by this OptionalBinder. */
    private boolean matchesTypeKey(Element element, Key<?> elementKey) {
      // Just doing .equals(typeKey) isn't enough, because the user can bind that themselves.
      return elementKey.equals(typeKey)
          && element instanceof ProviderInstanceBinding
          && (((ProviderInstanceBinding) element)
              .getUserSuppliedProvider() instanceof RealOptionalBinderProviderWithDependencies);
    }

    private boolean isInitialized() {
      return binder == null;
    }

    @Override public boolean equals(Object o) {
      return o instanceof RealOptionalBinder
          && ((RealOptionalBinder<?>) o).typeKey.equals(typeKey);
    }

    @Override public int hashCode() {
      return typeKey.hashCode();
    }

    /**
     * A base class for ProviderWithDependencies that need equality based on a specific object.
     */
    private abstract static class RealOptionalBinderProviderWithDependencies<T> implements
        ProviderWithDependencies<T> {
      private final Object equality;

      public RealOptionalBinderProviderWithDependencies(Object equality) {
        this.equality = equality;
      }

      @Override
      public boolean equals(Object obj) {
        return this.getClass() == obj.getClass()
            && equality.equals(((RealOptionalBinderProviderWithDependencies<?>) obj).equality);
      }

      @Override
      public int hashCode() {
        return equality.hashCode();
      }
    }
  }

  static class DefaultImpl extends BaseAnnotation implements Default {
    public DefaultImpl(String value) {
      super(Default.class, value);
    }
  }

  static class ActualImpl extends BaseAnnotation implements Actual {
    public ActualImpl(String value) {
      super(Actual.class, value);
    }
  }

  abstract static class BaseAnnotation implements Serializable, Annotation {

    private final String value;
    private final Class<? extends Annotation> clazz;

    BaseAnnotation(Class<? extends Annotation> clazz, String value) {
      this.clazz = checkNotNull(clazz, "clazz");
      this.value = checkNotNull(value, "value");
    }

    public String value() {
      return this.value;
    }

    @Override public int hashCode() {
      // This is specified in java.lang.Annotation.
      return (127 * "value".hashCode()) ^ value.hashCode();
    }

    @Override public boolean equals(Object o) {
      if (!(clazz.isInstance(o))) {
        return false;
      }

      BaseAnnotation other = (BaseAnnotation) o;
      return value.equals(other.value());
    }

    @Override public String toString() {
      return "@" + clazz.getName() + (value.isEmpty() ? "" : "(value=" + value + ")");
    }

    @Override public Class<? extends Annotation> annotationType() {
      return clazz;
    }

    private static final long serialVersionUID = 0;
  }
}