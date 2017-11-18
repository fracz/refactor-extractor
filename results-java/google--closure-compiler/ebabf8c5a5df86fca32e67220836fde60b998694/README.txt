commit ebabf8c5a5df86fca32e67220836fde60b998694
Author: blickly <blickly@google.com>
Date:   Tue Aug 16 10:20:46 2016 -0700

    Set the J2CL auto-detect pass to be considered off before it runs.

    This means that J2clSourceFileChecker.shouldRunJ2clPasses() will be false
    before J2clSourceFileChecker pass has run, rather than true, as it is now.

    Also refactored the name of the method to be positive ("shouldRunJ2clPasses")
    rather than negative ("shouldSkipExecution") just because I find that easier
    to follow.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=130416350