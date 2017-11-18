commit 9ffbeb20d25f87dfc56f2a2c784072bae7c2c626
Author: Henning Schmiedehausen <hgschmie@fb.com>
Date:   Tue Jun 25 17:33:37 2013 -0700

    re-introduce table writer predicate

    was dropped in refactoring, needed to prune already loaded partitions when running REFRESH MATERIALIZED VIEW.