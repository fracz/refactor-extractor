commit 98249507cff3e363ecb4c5f06f14f0bb96da1ad5
Author: Simon Willnauer <simonw@apache.org>
Date:   Wed Mar 9 09:38:46 2016 +0100

    Add missing index name to indexing slow log

    This was lost in refactoring even on the 2.x branch. The slow-log
    is not per index not per shard anymore such that we don't add the
    shard ID as the logger prefix. This commit adds back the index
    name as part of the logging message not as a prefix on the logger
    for better testabilitly.

    Closes #17025