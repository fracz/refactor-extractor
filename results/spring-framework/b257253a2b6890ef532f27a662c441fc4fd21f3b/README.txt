commit b257253a2b6890ef532f27a662c441fc4fd21f3b
Author: Phillip Webb <pwebb@vmware.com>
Date:   Wed Feb 20 10:32:53 2013 -0800

    Support for @Conditional configuration

    Introduce new @Conditional annotation that can be used to filter
    which @Configuration classes or methods are loaded. @Conditional
    can be used directly or as a meta-annotation. Condition implementations
    are provided via the 'Condition' interface and are free to filter based
    on any criteria available at the time that they run. The
    ConditionalContext provides access to the BeanDefinitionRegistry,
    Environment and ConfigurableListableBeanFactory along with a
    ResourceLoader and ClassLoader.

    The existing @Profile annotation has been refactored as a @Conditional
    with the added benefit that it can now be used as a method level
    annotation.