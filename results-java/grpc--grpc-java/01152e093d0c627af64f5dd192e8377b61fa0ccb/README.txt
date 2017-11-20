commit 01152e093d0c627af64f5dd192e8377b61fa0ccb
Author: ejona <ejona@google.com>
Date:   Fri Nov 14 12:46:39 2014 -0800

    Fix workaround in place for GFE's lack of trailers.

    A recent refactoring moved code so our previous workaround stopped
    producing any effect.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=79965647