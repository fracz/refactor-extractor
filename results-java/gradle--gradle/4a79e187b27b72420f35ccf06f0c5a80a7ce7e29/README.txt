commit 4a79e187b27b72420f35ccf06f0c5a80a7ce7e29
Author: Cedric Champeau <cedric@gradle.com>
Date:   Fri Jan 15 10:22:21 2016 +0100

    Attach the test suites to the `check` task consistenly in the native and JVM plugins.

    This commit refactors the `platform-native` module to extract the last bits of code that were specific to testing, in order to make it possible to reuse the same infrastructure to attach test suites to the `check` task in both the native and JVM worlds.

    Story: gradle/langos#113