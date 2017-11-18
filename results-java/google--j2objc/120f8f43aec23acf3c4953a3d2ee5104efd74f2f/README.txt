commit 120f8f43aec23acf3c4953a3d2ee5104efd74f2f
Author: kstanger <kstanger@google.com>
Date:   Tue Apr 7 07:28:22 2015 -0700

    Enum improvements:
    - Adds J2OBJC_ETERNAL_SINGLETON to all enum implementations, avoiding retain/release costs.
    - Makes all enum constructors private.
            Change on 2015/04/07 by kstanger <kstanger@google.com>
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=90508027