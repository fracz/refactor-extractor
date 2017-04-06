commit d872e2c5279a8a9827c0851218b1f4a641265639
Author: Ryan Ernst <ryan@iernst.net>
Date:   Mon Nov 16 09:15:31 2015 -0800

    Build: Use JDK at JAVA_HOME for compiling/testing, and improve build info output

    We currently enforce JAVA_HOME is set, but use gradle's java version to
    compile, and JAVA_HOME version to test. This change makes the compile
    and test both use JAVA_HOME, and clarifies which java version is used by
    gradle and that used for the compile/test.