commit 4c2aabdab895b4fc3a191682e32580b9796c0a46
Author: Cedric Champeau <cedric@gradle.com>
Date:   Mon Nov 6 18:12:39 2017 +0100

    Separate `ResolvedVersionConstraint` from `ImmutableVersionConstraint`

    This commit separates the "resolved" version constraint from its immutable definition. This will let us refactor
    the code to read immutable version constraints from cache, typically, without having to create a mutable version
    of them, or without having to arbitrarily pass through a version scheme.