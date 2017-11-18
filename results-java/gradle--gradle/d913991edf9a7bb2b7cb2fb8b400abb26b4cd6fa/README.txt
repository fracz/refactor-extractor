commit d913991edf9a7bb2b7cb2fb8b400abb26b4cd6fa
Author: Cedric Champeau <cedric@gradle.com>
Date:   Thu Nov 16 10:47:08 2017 +0100

    Improve test fixtures for resolution

    This commit improves the feature runner, so that the `RequiredFeatures` annotation can
    also be added on individual methods. It also improves the version spec to allow tweaking
    the published modules if necessary.

    Those changes are required to make #3394 possible.