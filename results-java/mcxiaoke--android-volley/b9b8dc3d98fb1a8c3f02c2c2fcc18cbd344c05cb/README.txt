commit b9b8dc3d98fb1a8c3f02c2c2fcc18cbd344c05cb
Author: Ficus Kirkpatrick <ficus@android.com>
Date:   Sat Nov 29 09:32:54 2014 -0800

    Migrate from Gradle to Maven.

    - Restructure source to src/{main,test} style
    - Add pom.xml and update Android.mk
    - Migrate all tests to JUnit4 and Robolectric
     - RequestQueueTest is currently @Ignored as fixing it will
       involve more extensive refactoring.
    - Main library still builds in Gradle; tests do not

    Change-Id: I1edc53bb1a54f64d3e806e4572901295ef63e2ca