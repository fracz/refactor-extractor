commit 6f31b356b2b21882b439324d233aceec413f4737
Author: alecpl <alec@alec.pl>
Date:   Wed Mar 31 07:14:32 2010 +0000

    - fix save/delete draft message with enabled threading (#1486596)
    - performance improvement using UID SEARCH intead of SEARCH + FETCH
    - re-fix r3445