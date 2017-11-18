commit b5b605a912a01a47b165fbf7f497ae6c3e9f395f
Author: Luke Daley <ld@ldaley.com>
Date:   Fri Mar 16 20:39:08 2012 +0000

    Fixes and more explicit modelling of “missing” artifact resolutions.

    Change the role of an ArtifactResolutionCache to include recording the fact that an artifact was previously not found at the given key. It did this before, and I removed it during refactoring unintentionally.