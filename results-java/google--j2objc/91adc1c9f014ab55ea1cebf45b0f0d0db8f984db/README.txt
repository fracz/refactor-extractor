commit 91adc1c9f014ab55ea1cebf45b0f0d0db8f984db
Author: Tom Ball <tball@google.com>
Date:   Tue Jul 9 12:11:26 2013 -0700

    Moves array access handling to ArrayRewriter.
    Adds reference getters for primitive array types to improve the readability and correctness of assignment translation.
    Adds unsigned right shift operator functions to JreEmulation so that the lhs expression is not evaluated twice.
            Change on 2013/07/02 by kstanger <kstanger@google.com>
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=48831677