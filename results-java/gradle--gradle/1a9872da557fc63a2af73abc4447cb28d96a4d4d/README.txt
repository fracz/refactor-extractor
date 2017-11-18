commit 1a9872da557fc63a2af73abc4447cb28d96a4d4d
Author: Luke Daley <ld@ldaley.com>
Date:   Fri Sep 5 18:34:04 2014 +1000

    Revert "Expansion/improvement of compile time transforming for model rules in the DSL."

    This reverts commit b6a2261e3ea52f22e34ca07b3e8493febe21d669.

    Need to deal with the unit tests that are using the model DSL, which is no longer feasible because it relies on transforms.