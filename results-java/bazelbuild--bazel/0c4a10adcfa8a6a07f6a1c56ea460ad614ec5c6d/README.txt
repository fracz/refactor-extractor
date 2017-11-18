commit 0c4a10adcfa8a6a07f6a1c56ea460ad614ec5c6d
Author: Chris Parsons <cparsons@google.com>
Date:   Tue Oct 11 18:54:12 2016 +0000

    Initial checkin of apple_dynamic_library

    This is at this point mostly an exercise in refactoring, as this initial
    implementation of apple_dynamic_library mirrors apple_binary exactly,
    except for the output extension and the -dynamiclib linker arg. There
    will be additional followup to deal with significant differences
    between these two rules.

    --
    MOS_MIGRATED_REVID=135822476