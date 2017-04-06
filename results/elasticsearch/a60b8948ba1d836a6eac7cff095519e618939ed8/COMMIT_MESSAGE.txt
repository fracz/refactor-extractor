commit a60b8948ba1d836a6eac7cff095519e618939ed8
Merge: 4844325 0a8afa2
Author: Jason Tedor <jason@tedor.me>
Date:   Mon Jun 6 10:59:28 2016 -0400

    Merge branch 'master' into feature/seq_no

    * master: (184 commits)
      Add back pending deletes (#18698)
      refactor matrix agg documentation from modules to main agg section
      Implement ctx.op = "delete" on _update_by_query and _reindex
      Close SearchContext if query rewrite failed
      Wrap lines at 140 characters (:qa projects)
      Remove log file
      painless: Add support for the new Java 9 MethodHandles#arrayLength() factory (see https://bugs.openjdk.java.net/browse/JDK-8156915)
      More complete exception message in settings tests
      Use java from path if JAVA_HOME is not set
      Fix uncaught checked exception in AzureTestUtils
      [TEST] wait for yellow after setup doc tests (#18726)
      Fix recovery throttling to properly handle relocating non-primary shards (#18701)
      Fix merge stats rendering in RestIndicesAction (#18720)
      [TEST] mute RandomAllocationDeciderTests.testRandomDecisions
      Reworked docs for index-shrink API (#18705)
      Improve painless compile-time exceptions
      Adds UUIDs to snapshots
      Add test rethrottle test case for delete-by-query
      Do not start scheduled pings until transport start
      Adressing review comments
      ...