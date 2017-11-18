commit a8e11250e72553dfaaf990a3b35addaa9045eb25
Author: emcmanus <emcmanus@google.com>
Date:   Tue Jul 7 15:13:28 2015 -0700

    In AutoValue, ensure that properties marked @Nullable get @Nullable on the corresponding constructor parameters and builder setters. Also, improve import behaviour for @Nullable and other annotations.

    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=97715265