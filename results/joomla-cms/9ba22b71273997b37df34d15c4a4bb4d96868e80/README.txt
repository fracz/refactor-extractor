commit 9ba22b71273997b37df34d15c4a4bb4d96868e80
Author: Frank Mayer <frank@frankmayer.net>
Date:   Wed May 31 23:16:33 2017 +0300

    Type safe comparison in plugins - second iteration (#13272)

    * Type safe string comparison of strings in plugins - second iteration
    - some more string comparisons
    - some bool
    - some int

    * Fixed two bugs introduced in previous commit (one involving multiple comparisons)

    * Missed a comparison. Thanks @shur!

    * Reversed type safe comparison, as it turns out, that the type of the value is not guaranteed.

    * Reversed type safe comparison (#2), as it turns out, that the type of the value is not guaranteed.

    * Reverted this one to not clash with @laoneo's refactoring efforts