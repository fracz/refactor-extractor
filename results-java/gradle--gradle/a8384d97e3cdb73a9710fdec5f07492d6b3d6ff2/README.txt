commit a8384d97e3cdb73a9710fdec5f07492d6b3d6ff2
Author: Cedric Champeau <cedric@gradle.com>
Date:   Thu Feb 2 09:55:31 2017 +0100

    Implement canonical mapping between spec components and merge operation

    In practice, thanks to the improvements in the merge algorithm, there are lots of cases where we can
    avoid creating a new `MergeOperation`, because the components (the 2 arrays of module exclusions) have
    already been found as a pair before. This commit introduces a canonical mapping for this pair thanks
    to identity hash maps. The main benefit is that we avoid computing the hash of the `MergeOperation`
    multiple times, which is costly when the arrays are "large".