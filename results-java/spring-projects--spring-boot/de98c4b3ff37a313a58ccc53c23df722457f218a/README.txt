commit de98c4b3ff37a313a58ccc53c23df722457f218a
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Sat Jan 21 16:07:30 2017 +0100

    Provide Reactor 3 auto-configuration

    This commit provides auto-configuration for the Reactor Core 3 library.
    A new configuration namespace, "spring.reactor" allows to configure
    hooks on operators, like "spring.reactor.stacktrace-mode.enabled".

    This property is enabled automatically by devtools, since it improves
    the developer experience and provides full stacktrace information when
    exceptions occur (but at a performance cost).

    Fixes gh-7302