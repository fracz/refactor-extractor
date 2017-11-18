commit 24f1f0ff964a88b35d5a9614c707905d0d9d5cd5
Author: Shay Banon <kimchy@gmail.com>
Date:   Fri Oct 14 15:20:38 2011 +0200

    improve refreshing logic to resync mappings on upgrade, reduce the amount of cluster events processing requires if the even if fired from several nodes / sources