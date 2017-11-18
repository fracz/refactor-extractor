commit cc993de9968bc77fa7674c797fd30a554b2c856f
Author: Areek Zillur <areek.zillur@elasticsearch.com>
Date:   Mon Aug 22 13:13:29 2016 -0400

    Simplify shard-level bulk operation execution

    This commit refactors execution of shard-level
    bulk operations to use the same failure handling
    for index, delete and update operations.