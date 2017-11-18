commit 4c6e1039b80859f17de5f3cbcfeba61ed8ea0485
Author: elbaum@google.com <elbaum@google.com@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Mon Sep 13 18:12:15 2010 +0000

    Support author scrubbing for HTML-format comments. Also refactors author scrubber to
    be a comment-oriented scrubber rather than a line-oriented scrubber, so only
    comments are processed.

    R=dbentley,dborowitz
    DELTA=263  (205 added, 42 deleted, 16 changed)


    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=213942


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@420 b0f006be-c8cd-11de-a2e8-8d36a3108c74