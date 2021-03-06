package com.google.inject.internal;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.internal.ProvisionListenerStackCallback.ProvisionCallback;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProviderWithExtensionVisitor;

/**
 * A {@link ProviderInstanceBindingImpl} for implementing 'native' guice extensions.
 *
 * <p>Beyond the normal binding contract that is mostly handled by our baseclass, this also
 * implements {@link DelayedInitialize} in order to initialize factory state.
 */
final class InternalProviderInstanceBindingImpl<T> extends ProviderInstanceBindingImpl<T>
    implements DelayedInitialize {
  private final Factory<T> originalFactory;

  InternalProviderInstanceBindingImpl(
      InjectorImpl injector,
      Key<T> key,
      Object source,
      Factory<T> originalFactory,
      InternalFactory<? extends T> scopedFactory,
      Scoping scoping) {
    super(
        injector,
        key,
        source,
        scopedFactory,
        scoping,
        originalFactory,
        ImmutableSet.<InjectionPoint>of());
    this.originalFactory = originalFactory;
  }

  @Override
  public void initialize(final InjectorImpl injector, final Errors errors) throws ErrorsException {
    originalFactory.source = getSource();
    originalFactory.provisionCallback = injector.provisionListenerStore.get(this);
    // For these kinds of providers, the 'user supplied provider' is really 'guice supplied'
    // So make our user supplied provider just delegate to the guice supplied one.
    originalFactory.delegateProvider = getProvider();
    originalFactory.initialize(injector, errors);
  }

  /**
   * A base factory implementation. Any Factories that delegate to other bindings should use the
   * {@code CyclicFactory} subclass, but trivial factories can use this one.
   */
  abstract static class Factory<T> implements InternalFactory<T>, Provider<T>, HasDependencies {
    private Object source;
    private Provider<T> delegateProvider;
    ProvisionListenerStackCallback<T> provisionCallback;

    /**
     * The binding source.
     *
     * <p>May be useful for augmenting runtime error messages.
     *
     * <p>Note: this will return {#code null} until {@link #initialize(InjectorImpl, Errors)} has
     * already been called.
     */
    final Object getSource() {
      return source;
    }

    /**
     * A callback that allows for implementations to fetch dependencies on other bindings.
     *
     * <p>Will be called exactly once, prior to any call to {@link #doProvision}.
     */
    abstract void initialize(InjectorImpl injector, Errors errors) throws ErrorsException;

    @Override
    public final T get() {
      Provider<T> local = delegateProvider;
      checkState(local != null,
          "This Provider cannot be used until the Injector has been created.");
      return local.get();
    }

    @Override public T get(
        final Errors errors,
        final InternalContext context,
        final Dependency<?> dependency,
        boolean linked)
        throws ErrorsException {
      if (!provisionCallback.hasListeners()) {
          return doProvision(errors, context, dependency);
        } else {
          return provisionCallback.provision(
              errors,
              context,
              new ProvisionCallback<T>() {
                @Override
                public T call() throws ErrorsException {
                  return doProvision(errors, context, dependency);
                }
              });
        }
    }
    /**
     * Creates an object to be injected.
     *
     * @throws com.google.inject.internal.ErrorsException if a value cannot be provided
     * @return instance to be injected
     */
    protected abstract T doProvision(
        Errors errors, InternalContext context, Dependency<?> dependency) throws ErrorsException;
  }

  /**
   * An base factory implementation that can be extended to provide a specialized implementation of
   * a {@link ProviderWithExtensionVisitor} and also implements {@link InternalFactory}
   */
  abstract static class CyclicFactory<T> extends Factory<T> {


    @Override
    public final T get(
        final Errors errors,
        final InternalContext context,
        final Dependency<?> dependency,
        boolean linked)
        throws ErrorsException {
      final ConstructionContext<T> constructionContext = context.getConstructionContext(this);
      // We have a circular reference between bindings. Return a proxy.
      if (constructionContext.isConstructing()) {
        Class<?> expectedType = dependency.getKey().getTypeLiteral().getRawType();
        @SuppressWarnings("unchecked")
        T proxyType =
            (T) constructionContext.createProxy(errors, context.getInjectorOptions(), expectedType);
        return proxyType;
      }
      // Optimization: Don't go through the callback stack if no one's listening.
      constructionContext.startConstruction();
      try {
        if (!provisionCallback.hasListeners()) {
          return provision(errors, dependency, context, constructionContext);
        } else {
          return provisionCallback.provision(
              errors,
              context,
              new ProvisionCallback<T>() {
                @Override
                public T call() throws ErrorsException {
                  return provision(errors, dependency, context, constructionContext);
                }
              });
        }
      } finally {
        constructionContext.removeCurrentReference();
        constructionContext.finishConstruction();
      }
    }

    private T provision(
        Errors errors,
        Dependency<?> dependency,
        InternalContext context,
        ConstructionContext<T> constructionContext)
        throws ErrorsException {
      try {
        T t = doProvision(errors, context, dependency);
        constructionContext.setProxyDelegates(t);
        return t;
      } catch (ErrorsException ee) {
        throw ee;
      } catch (Throwable t) {
        throw errors.withSource(getSource()).errorInProvider(t).toException();
      }
    }
  }
}