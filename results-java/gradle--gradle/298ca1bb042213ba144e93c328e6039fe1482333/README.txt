commit 298ca1bb042213ba144e93c328e6039fe1482333
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Nov 27 18:36:35 2012 +0100

    An attempt to improve the cache reuse test. Now it can be unignored because it takes under consideration the scenario when the artifact cache does not change between versions. The solution is far from ideal because BasicGradleDistribution.artifactCacheLayout property values have to be manually maintained.