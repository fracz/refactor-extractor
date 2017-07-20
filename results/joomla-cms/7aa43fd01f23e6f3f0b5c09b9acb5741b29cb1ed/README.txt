commit 7aa43fd01f23e6f3f0b5c09b9acb5741b29cb1ed
Author: Frank Mayer <frank@frankmayer.net>
Date:   Thu Dec 29 17:05:38 2016 +0200

    Some improvements in tests #2: (#13398)

    * Some improvements in tests #2:
    - fix callable name mismatches
    - removed closing tags (also one that's in other PR, to not clash)
    - removed loop that does nothing

    * Revert removal of empty loop, as $i is decremented in the while statement