commit 42acef37339afe6ac608c842f1637870ee9c4f6c
Author: Robert Greenwalt <robdroid@android.com>
Date:   Wed Aug 12 16:08:25 2009 -0700

    Add net type to mobile for mobile-required traffic

    This also refactors ConnectivityService a bit towards supporting multiple simultaneous connections by making each a seem like a seperate Network with it's own stateTracker, etc.
    Also adds tracking of process death to clean orphaned startUsingNetworkFeature features.