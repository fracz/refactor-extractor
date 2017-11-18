commit f9b554d7cf66b023c15707db8afae7adf1dbb7f0
Author: Cedric Champeau <cedric@gradle.com>
Date:   Fri Jul 7 16:59:30 2017 +0200

    Make use of the `japicmp` plugin to check for binary compatibility

    This commit introduces the use of the JApiCmp plugin to check for binary compatibility between releases of Gradle.
    The binary compatibility checks now generate a report per project, based on the last release version. Accepted
    changes need to be added to the `distributions/src/changes/changes-since-x.y.txt` file.

       - The build will fail if a non accepted, binary breaking change, is introduced.
       - Removes the binary breaking change test as it is superceded by the plugin use
       - Uses a single binary compatibility check task for all projects, which aggregates classpath

    This commit addresses a concern with regards to refactoring between modules. With individual reports, some errors would occur because
    we moved classes from one module to the other, while, in practice, all are shipped together (as of now). This means that you cannot,
    in practice, consider all artifacts as separate. They form a whole which is "the Gradle API", for good or for bad.

    The custom rules:
       - automatically accept changes based on our `@Incubating` policy
       - make sure that all rules declared in `changes-since.txt` are matched

    This plugins adds a sanity check, making sure that all the regressions that we declare
    as accepted in the `changes-since` file actually match a rule. If not, the build will
    fail, requiring to review this file.

    Binary compatibility checks is executed as part of `sanityCheck` and `check`.