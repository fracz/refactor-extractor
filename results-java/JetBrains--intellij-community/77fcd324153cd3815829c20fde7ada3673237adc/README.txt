commit 77fcd324153cd3815829c20fde7ada3673237adc
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Tue Dec 17 19:21:53 2013 +0400

    [log] Correctly load details of commits around the selected commit

    PROBLEM:
    To improve loading commit details we don't load it just for one
    selected commit: instead we take several commits before and after it
    in the table - and load details of all of them in a single command,
    and then save to the cache.

    However, we used to work only with Nodes in the graph. And when the
    log is filtered, and the graph is built only for the part of the log,
    there can be no Nodes for selected Hashes (IDEA-116718). Also commits
    which stay near each other in the filtered list, can stay far away in
    the graph => loading batch of Nodes around it won't affect commits
    which are really around selected one in the current table view
    (IDEA-116834).

    SOLUTION:
    * Declare AroundProvider interface which is to identify which commits
      are currently around the selected one.
    * Provide different implementations for logs with graph and without it.
    * Since graph model works with Nodes, and non-graph model works with
      Hashes, parametrize AroundProvider with the CommitId type.
    * Move load-around-stuff from DataGetter to the NodeAroundProvider,
      and a single DataGetter#getCommitData which works with different
      types of commit ids with the help of supplied AroundProvider
      (instead of separate implementations for Node and for Hash).