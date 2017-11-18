commit 4443123db935fafd6f51f9c1f03bba1bc6538a22
Author: Kush Chakraborty <kush@google.com>
Date:   Thu Mar 23 22:51:54 2017 +0000

    2nd attempt to split classpaths, and load test classes in a separate classloader. Currently this funcionality is hidden behind the "experimental_testrunner" flag.

    Original description (from commit 786cfa2ed980e278c42ee474408844f7e3720385):
    Separate the classpaths of the TestRunner with the test target, and use a separate Classloader to load the test target's classes. This enables a clean separation of the classes of the TestRunner with the target under test.

    This is achieved with the following steps:
    1. Start the test runner with only the bare bones classpaths to the Test Runner's classes which are used by the system ClassLoader.
    2. Have all the classpaths required to load the test target's classes in a TEST_TARGET_CLASSPATH environment variable exported by the stub script.
    3. Use a new classloader to load all the test target's classes using the paths in TEST_TARGET_CLASSPATH.

    This additionally enables the persistent test runner (currently experimental), to reload all the target's classes for every subsequent test run, so it can pick up any changes to the classes in between runs.

    The persistent test runner can be used by adding the argument
    --test_strategy=experimental_worker to the bazel test command (and having the tag "experimental_testrunner" in the java_test rule).
    Tested this against:
    1. gerrit/gerrit-common:client_tests: Dismal avg. improvement of 580ms to 557ms  (just 23ms)
    2. intellij/intellij/base:unit_tests: Somewhat modest avg. improvement 1661ms to 913ms (748 ms)

    --
    PiperOrigin-RevId: 151065529
    MOS_MIGRATED_REVID=151065529