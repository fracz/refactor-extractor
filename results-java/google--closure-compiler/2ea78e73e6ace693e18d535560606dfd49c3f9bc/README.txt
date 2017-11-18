commit 2ea78e73e6ace693e18d535560606dfd49c3f9bc
Author: nicksantos@google.com <nicksantos@google.com@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Mon Jan 31 16:08:25 2011 +0000

    Clean up type discovery by refactoring to the following algorithm.
    1) When we see a function literal, create the authoritative
    type for it.
    2) When we see an object literal, create the authoritative
    type for it.
    3) When we declare a symbol, check to see if it's assigned to
    an object or function literal, and use that type if it
    make sense to do so. Otherwise, fall back on the JSDoc info.
    This should make it a lot easier to give accurate types to object
    literal properties.
    I didn't intend to create any functional changes in this CL,
    but some minor ones were inevitable.

    R=johnlenz
    DELTA=618  (320 added, 249 deleted, 49 changed)


    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=392


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@739 b0f006be-c8cd-11de-a2e8-8d36a3108c74