commit 29ad9708781250ac39d81430bc13b0ea003fd24d
Author: Paul Merlin <paul@gradle.com>
Date:   Tue Nov 24 15:36:38 2015 +0100

    Rework StructSchemaModelExtractionSupport

    - Untangle property extraction loop, splitting it in steps, selecting
      properties first, validating them and finally extracting them.
    - Introduce ModelPropertyExtractionContext.
    - Extract MethodType and CandidateMethod concepts in dedicated types.
    - Improve readability of StructSchemaModelExtractionSupport and its
      managed/unmanaged implementations.
    - Limit data structures allocation during extraction.
    - Add a bunch of tests.

    This work is done after feedback items in the linked review.

    +review REVIEW-5695