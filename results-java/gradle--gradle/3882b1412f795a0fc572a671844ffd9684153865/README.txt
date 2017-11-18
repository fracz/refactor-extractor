commit 3882b1412f795a0fc572a671844ffd9684153865
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Fri Nov 23 14:54:41 2012 +0100

    improvements to PatternSet

    - added getAsIncludeSpec() and getAsExcludeSpec()
    - always access own fields directly rather than sometimes (not) using getters
    - use ArrayList instead of LinkedList
    - made fields final