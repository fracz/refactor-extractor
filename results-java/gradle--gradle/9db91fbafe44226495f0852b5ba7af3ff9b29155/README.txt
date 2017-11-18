commit 9db91fbafe44226495f0852b5ba7af3ff9b29155
Author: Cédric Champeau <cedric@gradle.com>
Date:   Tue May 10 23:16:17 2016 +0200

    Remove `DependencyHandler#project`

    This commit removes the recently added `project` method aimed at improving dependency configuration time.
    The "real" solution is going to come in 3.0 with improved "methodMissing" handling in the DSL.

    +review REVIEW-5952