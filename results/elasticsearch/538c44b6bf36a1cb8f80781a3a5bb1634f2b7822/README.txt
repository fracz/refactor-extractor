commit 538c44b6bf36a1cb8f80781a3a5bb1634f2b7822
Author: Ryan Ernst <ryan@iernst.net>
Date:   Sun Oct 12 19:04:59 2014 -0700

    Admin: Fix upgrade logic to work on lucene major version differences.

    Also improved upgrade tests, and added test against static ES 0.20 index
    which used Lucene 3.6.

    closes #8013