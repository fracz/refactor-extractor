commit 09c43c874354aa5c34e90b1ac2d5179bc8ea21b3
Author: Adrian Roos <roosa@google.com>
Date:   Thu Feb 9 19:58:25 2017 +0100

    Dependency: Make non-class dependencies type-safe

    Introduces a new DependencyKey<V> type which allows
    referring to non-class dependencies in a type-safe
    way. This also improves performance because we no
    longer need to perform string comparisons.

    Test: runtest systemui
    Change-Id: Idc948855a85a1899be41e7f0170c40e73e525024