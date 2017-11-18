commit 26fd2b3a8ebd16d47cb4291405cea1777a7ad0f2
Author: Goh Wei Xiang <xanec@users.noreply.github.com>
Date:   Thu Sep 28 13:02:05 2017 -0700

    Priority on loading for primary replica (#4757)

    * Priority on loading for primary replica

    * Simplicity fixes

    * Fix on skipping drop for quick return.

    * change to debug logging for no replicants.

    * Fix on filter logic

    * swapping if-else

    * Fix on wrong "hasTier" logic

    * Refactoring of LoadRule

    * Rename createPredicate to createLoadQueueSizeLimitingPredicate

    * Rename getHolderList to getFilteredHolders

    * remove varargs

    * extract out currentReplicantsInTier

    * rename holders to holdersInTier

    * don't do temporary removal of tier.

    * rename primaryTier to tierToSkip

    * change LinkedList to ArrayList

    * Change MinMaxPriorityQueue in DruidCluster to TreeSet.

    * Adding some comments.

    * Modify log messages in light of predicates.

    * Add in-method comments

    * Don't create new Object2IntOpenHashMap for each run() call.

    * Cache result from strategy call in the primary assignment to be reused during the same run.

    * Spelling mistake

    * Cleaning up javadoc.

    * refactor out loading in progress check.

    * Removed redundant comment.

    * Removed forbidden API

    * Correct non-forbidden API.

    * Precision in variable type for NavigableSet.

    * Obsolete comment.

    * Clarity in method call and moving retrieval of ServerHolder into method call.

    * Comment on mutability of CoordinatoorStats.

    * Added auxiliary fixture for dropping.