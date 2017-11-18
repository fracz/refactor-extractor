commit 86c5bddc6eced8adfb1c1e8f8e650be982e6a081
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Jun 19 05:58:51 2013 +0200

    Fixed the problem with with testRuntime and testCompile configurations resolved eagerly, even when no tests were present.

    1. Added @SkipWhenEmpty annotation where needed. Removed @Input annotation because it didn't add any value. Fixed the integration tests.
    2. This change may improve performance and heap consumption of certain builds (large multi project builds with little tests).