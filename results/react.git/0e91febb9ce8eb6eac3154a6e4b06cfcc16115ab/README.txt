commit 0e91febb9ce8eb6eac3154a6e4b06cfcc16115ab
Author: CommitSyncScript <jeffmo@fb.com>
Date:   Tue Jun 18 09:31:42 2013 -0700

    Better warnings for missing keys on arrays

    We have less dynamic arrays in the code base now so let's start warning for all
    the cases where we pass dynamic arrays without keys.

    I use the displayName to point out which component's render method was
    responsible. I only warn once per component. If the child was created in a
    different component (and passed as a property) I also show the owner of the
    child. Maybe it should've attached the key at a higher level.

    This does give false positives for arrays that are truly static. Those should
    probably be refactored to use the XML syntax if possible.