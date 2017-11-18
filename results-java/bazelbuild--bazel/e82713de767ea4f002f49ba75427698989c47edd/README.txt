commit e82713de767ea4f002f49ba75427698989c47edd
Author: Luis Fernando Pino Duque <lpino@google.com>
Date:   Tue Apr 26 16:22:38 2016 +0000

    Delete the interface NativeAspectFactory and make native aspects extend from NativeAspectClass.

    This a large refactoring of the aspects, currently we have the following:
    - AspectClasses: The interface AspectClass is a implemented by either
    SkylarkAspectClass or NativeAspectClass<NativeAspectFactory>.
    They are wrappers for the AspectFactories and they hold the information about
    the Class<> of the factory.
    - AspectFactories (FooAspect.java): Represented by the interfaces
    ConfiguredAspectFactory and NativeAspectFactory, also by
    the interface ConfiguredNativeAspectFactory which is the union of the two
    aforementioned interfaces.
    All aspects implement ConfiguredNativeAspectFactory except Skylark aspects
    which implement only ConfiguredAspectFactory.

    After this CL the distinction between NativeAspectFactories and NativeAspectClasses
    dissappear, namely aspect that extends NativeAspectClass is considered native
    and if it implements ConfiguredAspectFactory it is configured.
    Therefore the interfaces NativeAspectFactory and ConfiguredNativeAspectFactory
    both disappear.

    With this refactoring the aspectFactoryMap in the ConfiguredRuleClassProvider
    changes its type from (String -> Class<? extends NativeAspectClass>)
    to (String -> NativeAspectClass) which means it is now able to have an instance
    of the aspect instead of its Class only.
    By doing this, it is now possible to pass parameters when creating an
    aspect in the ConfiguredRuleClassProvider.

    --
    MOS_MIGRATED_REVID=120819647