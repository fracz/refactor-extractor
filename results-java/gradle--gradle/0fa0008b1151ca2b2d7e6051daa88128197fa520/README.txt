commit 0fa0008b1151ca2b2d7e6051daa88128197fa520
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Sep 8 23:57:09 2011 +0200

    Some necessary refactorings in the Daemon. The logic that operates on the daemon registry now shares the same synchronization mechanism (e.g. CompletionHandler). This way we avoid certain concurrency issues.