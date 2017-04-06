commit 14f45c17847723bf9968d5a7d0753f4bb4b9463f
Merge: 1988b8b 697174d
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Mar 22 13:46:50 2016 +0100

    Merge pull request #17146 from cbuescher/sort-add-build

    For the refactoring of SortBuilders related to #10217, each SortBuilder needs to get a build()
    method that produces a SortField according to the SortBuilder parameters on the shard.