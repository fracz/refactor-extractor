commit 60d2c63a3058474f853103bbc3d63ad0ed9f10c4
Author: johnlenz <johnlenz@google.com>
Date:   Mon Oct 30 17:36:04 2017 -0700

    Rollforward of "Improve the compile time cost for Optimize Calls"

    Has the following changes from the original:

     - fixed bad code, when a symbol definition had a HOOK or other nontrvial
       expressions (a mismatch between isCandidate and ReferenceMap::getFunctionNodes).
     - fixes regressions where constructor parameters were not being optimized if used in the target of a property set (f.prototype.method = ...).
     - fixes regressions where parameters were not optimized if the symbol was used in a typeof/instanceof expression
     - reverts improvement to constructor optimizations when caused breakage when function constructor properties were used to invoke constructors (until we can decide to ban them or similar).

    Improve the compile time cost for Optimize Calls from #1 to #17 (roughly 25% of its previous time).

    Instead of using the DefinitionUseSiteFinder, as simpler data structure is introduced: OptimizeCalls.ReferenceMap.  The ReferenceMap provides...

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=173976261