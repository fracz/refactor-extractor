commit 75a91c23ab2822145b2b89bc847a6947e0462a72
Author: Ben Manes <ben.manes@gmail.com>
Date:   Thu Oct 29 20:50:39 2015 -0700

    W-TinyLfu now means segmented main space

    Based on the analysis this is an improvement of the original version's
    LRU main space. That version is now `Simple W-TinyLfu`. The improvement
    is reflected in the updated stats and in the upcoming paper.