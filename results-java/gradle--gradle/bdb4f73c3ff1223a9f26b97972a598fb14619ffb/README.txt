commit bdb4f73c3ff1223a9f26b97972a598fb14619ffb
Author: Cedric Champeau <cedric@gradle.com>
Date:   Wed Jan 6 10:45:18 2016 +0100

    Introduce a test suite specific resolve context.

    This is the first step in building a test suite specific runtime classpath. By having a dedicated `ResolveContext`, we're able to describe that the dependencies for executing a test suite are not necessarily the same as the dependencies required to compile the test suite. While this change introduces such a resolve context, it is not yet capable of describing the fact that we want to depend on a runtime jar instead of an API jar. Therefore, this commit is just preliminary refactoring to allow such a feature.

    Story: gradle/langos#127