commit 356e7474c9ede3c45232b39e895d554349a55328
Author: ddebree <ddebree@gmail.com>
Date:   Thu Aug 13 13:41:36 2015 +0200

    Refactored TimeCacheMap to extend RotatingMap.
    This should allow current users of the TimeCacheMap to progressively migrate off the deprecated TimeCacheMap to the simpler RotatingMap.
     This also improves code reuse since many of the methods were shared.