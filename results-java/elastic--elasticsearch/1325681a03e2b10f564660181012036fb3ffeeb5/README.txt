commit 1325681a03e2b10f564660181012036fb3ffeeb5
Author: Simon Willnauer <simonw@apache.org>
Date:   Thu May 25 15:02:43 2017 +0200

    Fix error message if an incompatible node connects (#24884)

    This message broken in recent refactoring, this commit also adds a
    basic unit-test to ensure we maintain the correct version.