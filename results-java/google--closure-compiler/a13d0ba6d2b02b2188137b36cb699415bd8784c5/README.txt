commit a13d0ba6d2b02b2188137b36cb699415bd8784c5
Author: dimvar <dimvar@google.com>
Date:   Wed Jun 14 22:39:08 2017 -0700

    Follow-up to 23e2bf3ffd8b5761b6ea1fa4a3b4afe70982da1a (Migrate TypedCodePrinter to NTI).

    The only change in functionality is in Compiler.java, to avoid using an OTI registry with NTI. The rest is refactoring and a new unit test.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=159068282