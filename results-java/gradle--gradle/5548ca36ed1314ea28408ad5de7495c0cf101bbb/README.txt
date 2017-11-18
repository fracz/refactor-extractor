commit 5548ca36ed1314ea28408ad5de7495c0cf101bbb
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Nov 27 19:22:28 2012 +0100

    Missed commit to an attempt to improve the cache reuse test. Now it can be unignored because it takes under consideration the scenario when the artifact cache does not change between versions. The solution is far from ideal because BasicGradleDistribution.artifactCacheLayout property values have to be manually maintained.