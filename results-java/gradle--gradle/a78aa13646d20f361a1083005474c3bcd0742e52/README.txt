commit a78aa13646d20f361a1083005474c3bcd0742e52
Author: Cedric Champeau <cedric@gradle.com>
Date:   Fri Oct 21 11:30:15 2016 +0200

    Adds a clearer definition of `ConfigurationRole`

    This commit refactors `ConfigurationRole` in order to have a clearer definition of why it is necessary.
    We now define 3 cases:

    * a configuration is intented to be used both for building and consuming artifacts (`FOR_BUILDING_OR_PUBLISHING`)
    * a configuration is only intended to be used when building a project (`FOR_BUILDING_ONLY`)
    * a configuration is only intended to be used when an upstream dependency consumes a project (`FOR_PUBLISHING_ONLY`)

    This refactor doesn't change the tests: the default is now `FOR_BUILDING_ONLY`, but for backwards compatibility it
    should really be `FOR_BUILDING_OR_PUBLISHING`, which means that in most cases the set of incoming and outgoing dependencies
    is the same. This default is going to be changed in a subsequent commit, with more test coverage.

    See: https://github.com/gradle/performance/issues/135