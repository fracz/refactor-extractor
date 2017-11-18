commit 4d09ce45270187fa65a891e56081960a0ae449e5
Author: Nicholas.J.Santos <Nicholas.J.Santos@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Thu Jul 15 20:03:22 2010 +0000

    Change on 2010/07/14 14:52:38 by nicksantos

            Automated g4 rollback.

            *** Reason for rollback ***

            breaks contentads

            *** Original change description ***

            Replace direct references to stripped types with 'void 0'.

            R=dcc
            DELTA=107  (0 added, 100 deleted, 7 changed)

    Change on 2010/07/14 17:36:48 by acleung

            Fix GraphColoringTest for future JDK changes.

            R=nicksantos
            DELTA=7  (0 added, 6 deleted, 1 changed)

    Change on 2010/07/14 18:11:13 by johnlenz

            Minor cleanup of the CodeConsumer interface.

            R=acleung
            DELTA=180  (14 added, 128 deleted, 38 changed)

    Change on 2010/07/14 18:13:53 by johnlenz

            Minor performance improvement for quoted strings that may need to be
            escaped.

            R=nicksantos
            DELTA=1  (0 added, 0 deleted, 1 changed)

    Change on 2010/07/14 18:34:27 by pkeslin

            Move the Image constructor from deprecated.js to html5 since it seems that
            it has been formalized as a part of that spec and is the constructor for
            the HTMLImageElement.

            R=acleung
            DELTA=27  (7 added, 18 deleted, 2 changed)

    Change on 2010/07/14 19:11:18 by pkeslin

            Automated g4 rollback of changelist 16438831.

            *** Reason for rollback ***

              Original change breaks the closure build.

            *** Original change description ***

            Move the Image constructor from deprecated.js to html5 since it seems that
            it has been formalized as a part of that spec and is the constructor for
            the HTMLImageElement.

            R=acleung
            DELTA=27  (18 added, 7 deleted, 2 changed)


    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=52001


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@278 b0f006be-c8cd-11de-a2e8-8d36a3108c74