commit 831be0a9c003beb14645906357c31920c5888423
Author: dimvar <dimvar@google.com>
Date:   Fri Nov 13 15:26:53 2015 -0800

    More refactoring to simplify DisambiguateProperties.
    Remove the custom scoping callback class.
    Inline the type system class.
    Restored some jsdocs accidentally deleted in a previous CL.
    No perf impact.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=107819553