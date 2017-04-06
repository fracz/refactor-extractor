commit 4a3978d1b7107f04bf55214678caf6b049c3d3c9
Author: Shay Banon <kimchy@gmail.com>
Date:   Sat Dec 7 19:20:16 2013 +0100

    Optimize dynamic mapping updates on master by processing latest one per index/node
    Instead of processing all the bulk of update mappings we have per index/node, we can only update the last ordered one out of those (cause they are incremented on the node/index level). This will improve the processing time of an index that have large updates of mappings.
    closes #4373