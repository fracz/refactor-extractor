commit 00044246bb048848ff8badef84c35290a52dd1af
Author: Michael Hoisie <hoisie@google.com>
Date:   Wed May 24 17:12:19 2017 -0700

    Update RoboJavaModulePlugin to use Java 8

    Previously, RoboJavaModulePlugin declared VERSION_1_7 for both
    sourceCompatibility and targetCompatibility. Now that Robolectric only
    supports Java 8, the source and target compatibility can be updated to
    8. This allows Robolectric to use Java 8 features such as lambda
    expressions and method references.

    The main changes involved uses of AssertJ's generic assertThat method.
    Java 8 has improved type inference, so the compiler complained about
    assertions where it could not infer the type.