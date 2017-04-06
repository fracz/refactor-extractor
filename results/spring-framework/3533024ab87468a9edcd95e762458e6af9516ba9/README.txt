commit 3533024ab87468a9edcd95e762458e6af9516ba9
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Thu Oct 13 20:55:42 2016 +0200

    Add EnableWebReactive + WebReactiveConfigurer

    This commit improves the existing web reactive configuration
    infrastructure with the following changes:

    * renamed `WebReactiveConfiguration` to
    `WebReactiveConfigurationSupport` and is is no longer a Configuration
    class
    * created the `WebReactiveConfigurer` interface; Configuration classes
    implementing it will augment the web reactive configuration support
    * created the `DelegatingWebReactiveConfiguration` and
    `WebReactiveConfigurerComposite` to effectively tie those custom-defined
    configurers to the main configuration support
    * created the `@EnableWebReactive` to active that support in
    configuration classes

    Issue: SPR-14754