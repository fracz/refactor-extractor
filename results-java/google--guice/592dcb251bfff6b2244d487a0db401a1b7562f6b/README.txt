commit 592dcb251bfff6b2244d487a0db401a1b7562f6b
Author: lukes <lukes@google.com>
Date:   Thu Aug 18 09:30:27 2016 -0700

    Rewrite RealOptionalBinder to use the InternalFactory interfaces and avoid the overheads of normal ProviderInstanceBindings + Providers.

    A couple of notes on the implementation
    * all the mutable state (besides Binder) has been moved into a new helper object 'BindingSelection'.  I think by moving things into a helper object the interactions are a bit clearer.  This also allows us to make all the factories static.
    * calculating the keys + optional binder 'name' have been delayed until configure() time or until setDefault()/setBinding() is called.  I would assume that most optionalbinders are deduped or that setDefault()/setBinding() is not called, so we can avoid work in the common cases.
    * None of the factories need circular deps resolution because they simply delegate to other factories. (in this way they are much like the ProxyFactory used for LinkedKeyBindings, we just wrap the result in Optional)
    * I performed a minor refactoring the the java.util.Optional reflection helpers

    Benchmark results:
    timeOptionalBinderInjection measures how long to inject an object that depends on other OptionalBinders in every configuration
    timeProvidesInjection times how long it takes to inject an @Provides method
    timeOptionalBinderSetBinding times how long it takes to directly inject an OptionalBinder.setBinding() binding that delegates to the same bindings as 'timeProvidesInjection'.  So we should be able to see the overhead of OptionalBinder to fetch the same object.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=130648042