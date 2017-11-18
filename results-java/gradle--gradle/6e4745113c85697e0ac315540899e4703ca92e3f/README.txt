commit 6e4745113c85697e0ac315540899e4703ca92e3f
Author: Cedric Champeau <cedric@gradle.com>
Date:   Wed Dec 9 17:21:10 2015 +0100

    Introduce `testing-base` module

    This commit introduces a new `testing-base` module aimed at detangling the `plugins` module, by extracting 2 things:

    * classes that are used independently of a testing framework or the JVM (`TestDescriptor`, ...)
    * classes which are specific to JVM testing (`Test`, `TestReport`, `TestWorker`, ...)

    The first category are extracted in the `testing-base` module. The second category have been migrated to the `testing-jvm` module, which now includes TestNG specific classes too.

    * The `testing-jvm` module no longer depends on `plugins`, but on `testing-base` instead.
    * The `plugins` module now depends on `testing-jvm` (so we have effectively inverted the dependency).

    It's worth noting that while main classes have been shuffled around, test classes have not been moved, and some quality checks had to be disabled. For example, strict compilation and classcycle cannot be used anymore in the `testing-jvm` module without introducing breaking changes.
    Two classes (`JUnitOptions` and `TestNGOptions`) have been migrated from Groovy to Java.

    At this point, building Gradle is broken. Subsequent commits will fix that.

    Story: gradle/langos#103
    Item: refactor-plugins