commit d1f6672a58a4d804e7f164a3c2311570dd9cb2a0
Author: Chris Beams <cbeams@vmware.com>
Date:   Wed Nov 16 04:21:28 2011 +0000

    Refactor ImportSelector support

    Separate concerns of @Configuration class selection from the need to
    register certain infrastructure beans such as auto proxy creators.

    Prior to this change, ImportSelector implementations were responsible
    for both of these concerns, leading to awkwardness and duplication.

    Also introduced in this change is ImportBeanDefinitionRegistrar and
    two implementations, AutoProxyRegistrar and AspectJAutoProxyRegistrar.
    See the refactored implementations of CachingConfigurationSelector,
    TransactionManagementConfigurationSelector to see the former;
    AspectJAutoProxyConfigurationSelector to see the latter.

    ImportSelector and ImportBeanDefinitionRegistrar are both handled as
    special-case arguments to the @Import annotation within
    ConfigurationClassParser.

    These refactorings are important because they ensure that Spring users
    will be able to understand and extend existing @Enable* annotations
    and their backing ImportSelector and @Configuration classes, as well
    as create their own with a minimum of effort.