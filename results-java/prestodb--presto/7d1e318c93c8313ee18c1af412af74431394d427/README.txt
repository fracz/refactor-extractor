commit 7d1e318c93c8313ee18c1af412af74431394d427
Author: Haozhun Jin <haozhun.jin@gmail.com>
Date:   Tue Jun 20 20:53:31 2017 -0700

    Remove intermediate BlockBuilder in MongoPageSource

    by using BlockBuilder.beginBlockEntry

    This improves performance. It also migrates presto-mongodb
    to new map block.