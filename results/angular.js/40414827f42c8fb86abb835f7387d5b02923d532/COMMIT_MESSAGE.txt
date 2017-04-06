commit 40414827f42c8fb86abb835f7387d5b02923d532
Author: Buu Nguyen <buunguyen@gmail.com>
Date:   Tue Sep 24 21:59:47 2013 -0500

    docs($compile): improve explanation of Attributes.$observe

    The current comment of Attributes.$observe doesn't state correctly the behavior when the attribute contains no interpolation. Specifically, it states that the observer function will never be invoked if the attribute contains no interpolation. However, the actual behavior in this case is that the observer will be invoked once during the next digest loop.