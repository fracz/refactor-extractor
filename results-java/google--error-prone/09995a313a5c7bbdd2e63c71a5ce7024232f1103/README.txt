commit 09995a313a5c7bbdd2e63c71a5ce7024232f1103
Author: cpovirk <cpovirk@google.com>
Date:   Thu Sep 4 07:32:36 2014 -0700

    Stop duplicating information between the result value and the store. New policy:
    - Local variables go into the store (setInformation) and come out of the store (getInformation).
    - Everything else goes into the result value ("return new RegularTransferResult<>(VALUE, ...))") and comes out of TransferInput.getValueOfSubNode.

    This saves us the trouble of keeping the two in sync and of worrying what happens if we don't. In particular, update() and updateConditional() are now trivial.

    Along the way, improve things around |final| variables referenced by local/anonymous classes:
    - Recognize that they're different variables than any variables of the same name inside the class.
    - Check whether they have constant values available.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=74775700