commit 87e2ecad73b20fa22fc06038ced2da080e650c5a
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Sun Jan 22 18:38:56 2012 +0400

    Git Branch Operations refactoring.

    Instead of being run via GitMultiRootOperationExecutor and handling different situations with hacks, let each GitBranchOperation control its execution by itself, using a number of helper and common methods in the common class GitBranchOperation.