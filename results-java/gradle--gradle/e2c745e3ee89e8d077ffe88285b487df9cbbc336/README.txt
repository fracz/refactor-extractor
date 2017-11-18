commit e2c745e3ee89e8d077ffe88285b487df9cbbc336
Author: Cedric Champeau <cedric@gradle.com>
Date:   Mon Feb 1 11:34:53 2016 +0100

    Implement faster class node resolution for Gradle classes.

    This commit introduces a custom `ResolveVisitor` for Gradle, called `GradleResolveVisitor`, which takes advantage of the fact that Gradle knows exactly which classes are possibly imported in a script. We can use that information to shortcut some resolution execution pathes, and significantly improve compilation performance. "first build" performance test improves by 20% after this commit.

    +review REVIEW-5811