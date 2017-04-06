commit df7ad9970b42d15e377130966054070574016316
Author: Jason Tedor <jason@tedor.me>
Date:   Mon Jul 11 08:30:09 2016 -0400

    Batch process node left and node failure

    Today when a node is removed the cluster (it leaves or it fails), we
    submit a cluster state update task. These cluster state update tasks are
    processed serially on the master. When nodes are removed en masse (e.g.,
    a rack is taken down or otherwise becomes unavailable), the master will
    be slow to process these failures because of the resulting reroutes and
    publishing of each subsequent cluster state. We improve this in this
    commit by processing the node removals using the cluster state update
    task batch processing framework.

    Relates #19289