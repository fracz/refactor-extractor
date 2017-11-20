commit 621f87fcbcd4f4262fa7f9f5b6e0e259c75d74b2
Author: Jesse Vincent <jesse@fsck.com>
Date:   Sat Nov 27 04:03:15 2010 +0000

    Revert "refactor getHeaders and removeHeaders to use a common method and an"

    Subtly flawed. Returned the wrong headers

    This reverts commit 657b3961f86b1b694fb7587216ecefeb0b20f5d2.