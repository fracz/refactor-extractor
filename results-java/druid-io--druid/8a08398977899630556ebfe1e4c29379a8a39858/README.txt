commit 8a08398977899630556ebfe1e4c29379a8a39858
Author: Dave Li <d_vd415@hotmail.com>
Date:   Fri Jun 24 17:52:19 2016 -0400

    Add segment pruning based on secondary partition dimension (#2982)

    * add get dimension rangeset to filters

    * add get domain to ShardSpec and added chunk filter in caching clustered client

    * add null check and modified not filter, started with unit test

    * add filter test with caching

    * refactor and some comments

    * extract filtershard to helper function

    * fixup

    * minor changes

    * update javadoc