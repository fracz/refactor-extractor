commit 44c1de70de3054dee1be6232b2718d6fea8856c7
Author: Martin Traverso <martint@fb.com>
Date:   Wed Dec 3 23:41:39 2014 -0800

    Minor improvements to ImmutableCollectors

    - Rename methods to toImmutableList and toImmutableSet
    - Simplify method signatures
    - Remove unnecessary type specification for lambda args
    - Declare set collector as UNORDERED to give stream implementation
      more room for optimization