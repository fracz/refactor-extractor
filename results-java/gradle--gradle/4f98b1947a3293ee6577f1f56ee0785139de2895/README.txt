commit 4f98b1947a3293ee6577f1f56ee0785139de2895
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Dec 7 10:57:51 2012 +0100

    REVIEW-646 and more, refactoring in the dependency resolve actions

    1. Moved the responsibility of constructing the aggregate dependency resolve action to the ResolutionStrategy. Needed to move the ModuleForcingResolveAction to core.
    2. Rename job for the implementation of DependencyResolveDetails.
    3. Some optimization in the ModuleForcingResolveAction. Not sure if it is needed but it was cheap.