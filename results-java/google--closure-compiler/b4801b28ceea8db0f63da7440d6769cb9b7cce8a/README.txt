commit b4801b28ceea8db0f63da7440d6769cb9b7cce8a
Author: Nicholas.J.Santos <Nicholas.J.Santos@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Sat May 29 20:07:02 2010 +0000

    Change on 2010/05/28 by johnlenz

            Inline functions with inner functions into global scope if they don't
            declare any names.

            R=acleung
            DELTA=329  (244 added, 27 deleted, 58 changed)

    Change on 2010/05/28 by dcc

            First pass on refactoring FoldConstants into separate peephole optimizations. This changelist adds the notion of an AbstractPeepholeOptimization and adds a PeepholeOptimizationsPass that runs them. In this go around the minimization aspects of FoldConstants have been factored out into PeepholeMinimize. A future change will factor out the other components of FoldConstants into their own PeepholeOptimizations. DefaultPassConfig has also been updated to run the new PeepholeOptimizationPass.

            This change does not move the minimization-related unit tests from FoldConstantsTest to PeepholeMinimizeTest -- a future, separate, change will do so. Hopefully this will make it easier to review.

            R=acleung
            DELTA=1966  (1271 added, 675 deleted, 20 changed)



    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=13010


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@235 b0f006be-c8cd-11de-a2e8-8d36a3108c74