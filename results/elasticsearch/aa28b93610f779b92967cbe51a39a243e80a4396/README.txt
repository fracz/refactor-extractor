commit aa28b93610f779b92967cbe51a39a243e80a4396
Author: kimchy <kimchy@gmail.com>
Date:   Sun Aug 22 02:47:34 2010 +0300

    refactor how throttling is done, instead of doing it after a shard is allocated to a node, and then wait till its allowed to recover, do it on the allocation level, and don't allocate a shard to a node that has N number of recoveries going on it