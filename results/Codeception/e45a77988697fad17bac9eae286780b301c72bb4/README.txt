commit e45a77988697fad17bac9eae286780b301c72bb4
Author: Michael Bodnarchuk <DavertMik@users.noreply.github.com>
Date:   Sun May 21 01:17:54 2017 +0300

    [WIP] Suites config flexibility (#4214)

    * Allow to set a custom path to suite
    Allow to have tests without actors

    * updated generators to exclude actors if not provided
    improved tests

    * added _support directory

    * replaced class_name with actor in suite config

    * replaced actor with actorSuffix for global config

    * renamed actorSuffix to actor_suffix

    * fixed tests

    * codefixes

    * fixed hhvm test

    * fixed tests