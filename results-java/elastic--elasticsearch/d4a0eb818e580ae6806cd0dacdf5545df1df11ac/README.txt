commit d4a0eb818e580ae6806cd0dacdf5545df1df11ac
Author: Britta Weber <britta.weber@elasticsearch.com>
Date:   Fri May 9 17:54:28 2014 +0200

    refactor: make requiredSize, shardSize, minDocCount and shardMinDocCount a single parameter

    Every class using these parameters has their own member where these four
    are stored. This clutters the code. Because they mostly needed together
    it might make sense to group them.