commit 2a9d89d3a5148bf006e674e1bf002159eea03053
Author: bradfordcsmith <bradfordcsmith@google.com>
Date:   Wed Jun 8 09:56:34 2016 -0700

    Add a Compiler option to print the compiler configuration to stderr when it is
    initialized.

    The purpose of this change is to make it possible to check the actual options
    that affect the compiler's behavior for a given run once all flags and defaults
    are handled.

    The formatting can still use a lot of improvement, but this is a start.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=124362642