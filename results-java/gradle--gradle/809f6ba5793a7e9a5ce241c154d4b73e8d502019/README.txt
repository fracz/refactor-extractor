commit 809f6ba5793a7e9a5ce241c154d4b73e8d502019
Author: Cedric Champeau <cedric@gradle.com>
Date:   Tue Dec 6 18:06:55 2016 +0100

    Add support for publishing libraries using the `maven-publish` plugin

    This commit adds support for API dependencies in the generated pom file when using the `maven-publish` plugin.
    It refactors the `Usage` class to make it public, and extensible by users. Then the legacy internal `Usage` class,
    which was only used by the publishing plugin, has been updated to use `Usage` as a member, instead of being directly
    the usage. This allows us to map configurations to usages more precisely.

    - The `compile` scope of the generated pom file consists of the `api` dependencies.
    - The `runtime` scope of the generated pom file consists of the old `runtime` dependencies *and* `runtimeElements` dependencies

    With this setup, the new publishing plugin is now closer to the reality than the old Maven publishing plugin.