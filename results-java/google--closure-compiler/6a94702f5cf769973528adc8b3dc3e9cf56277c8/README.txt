commit 6a94702f5cf769973528adc8b3dc3e9cf56277c8
Author: Nicholas.J.Santos <Nicholas.J.Santos@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Thu Jul 8 23:28:37 2010 +0000

    Change on 2010/07/07 15:59:31 by johnlenz

            Automated g4 rollback.

            *** Reason for rollback ***

              If an output file is openned, close it to be sure it is flushed.

            *** Original change description ***

            Automated g4 rollback.

            *** Reason for rollback ***

              <enter reason for rollback>

            *** Original change description ***

            Buffer file stream to improve file system performance.

            R=acleung
            DELTA=36  (26 added, 1 deleted, 9 changed)

    Change on 2010/07/07 16:31:41 by nicksantos

            Sort and namespace the baked-in externs files, so that
            they do not conflict with the user's file names.
            Fixes issue 194

            R=acleung
            DELTA=90  (69 added, 17 deleted, 4 changed)


    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=50002


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@266 b0f006be-c8cd-11de-a2e8-8d36a3108c74