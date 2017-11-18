commit f98c96cf740c3c75ba72bac352eb44dbc4c6b2b2
Author: Cedric Champeau <cedric@gradle.com>
Date:   Mon Jan 18 16:45:52 2016 +0100

    Refactor JVM test suite rules to better fit the model standards

    This commit refactors the JVM test suites creation rules in order to avoid delegation to static methods whenever possible. There is, however, an expression problem when it comes to determine the type of the binary, so not all delegation can be removed yet.

    Story: gradle/langos#113