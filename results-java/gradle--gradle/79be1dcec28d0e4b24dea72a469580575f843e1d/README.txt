commit 79be1dcec28d0e4b24dea72a469580575f843e1d
Author: Rene Groeschke <rene@breskeby.com>
Date:   Fri Dec 14 16:47:33 2012 +0100

    Some refactoring to move more away from ivy
    - Change ModuleVersionResolveResult#getId to return ModuleVersionIdentifier instead of ModuleRevisionId.
    - Change ModuleVersionIdResolveResult#getId to return ModuleVersionIdentifier instead of ModuleRevisionId.