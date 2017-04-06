commit 598854dd72d7fb01a7e26a9dad065de3deaa5eb7
Merge: 889db1c 34f4ca7
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Mon Sep 1 16:13:57 2014 +0200

    [Discovery] accumulated improvements to ZenDiscovery

    Merging the accumulated work from the feautre/improve_zen branch. Here are the highlights of the changes:

    __Testing infra__
    - Networking:
        - all symmetric partitioning
        - dropping packets
        - hard disconnects
        - Jepsen Tests
    - Single node service disruptions:
        - Long GC / Halt
        - Slow cluster state updates
    - Discovery settings
        - Easy to setup unicast with partial host list

    __Zen Discovery__
    - Pinging after master loss (no local elects)
    - Fixes the split brain issue: #2488
    - Batching join requests
    - More resilient joining process (wait on a publish from master)

    Closes #7493