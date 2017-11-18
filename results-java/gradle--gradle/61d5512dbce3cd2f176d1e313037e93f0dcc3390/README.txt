commit 61d5512dbce3cd2f176d1e313037e93f0dcc3390
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu May 30 00:25:02 2013 +0200

    Bumped the cache layout. The protocol of the information we keep in the artifact cache file lock changes. The artifact cache lock is based on the artifact directory (e.g. artifacts-24). Since the format of the file lock changes, we need to use different dir for artifacts (e.g. artifacts-25). This is required after recent performance improvements to the file lock.