commit 52d7f742266a5062591798dde72e9f38a5d388de
Author: Jihoon Son <jihoonson@apache.org>
Date:   Wed Oct 18 15:24:08 2017 +0900

    Add streaming aggregation as the last step of ConcurrentGrouper if data are spilled (#4704)

    * Add steaming grouper

    * Fix doc

    * Use a single dictionary while combining

    * Revert GroupByBenchmark

    * Removed unused code

    * More cleanup

    * Remove unused config

    * Fix some typos and bugs

    * Refactor Groupers.mergeIterators()

    * Add comments for combining tree

    * Refactor buildCombineTree

    * Refactor iterator

    * Add ParallelCombiner

    * Add ParallelCombinerTest

    * Handle InterruptedException

    * use AbstractPrioritizedCallable

    * Address comments

    * [maven-release-plugin] prepare release druid-0.11.0-sg

    * [maven-release-plugin] prepare for next development iteration

    * Address comments

    * Revert "[maven-release-plugin] prepare for next development iteration"

    This reverts commit 5c6b31e488c413073e2b1d4ce128bdff0649b41a.

    * Revert "[maven-release-plugin] prepare release druid-0.11.0-sg"

    This reverts commit 0f5c3a8b82415b34fa765dc375d87ae8fe4daa3b.

    * Fix build failure

    * Change list to array

    * rename sortableIds

    * Address comments

    * change to foreach loop

    * Fix comment

    * Revert keyEquals()

    * Remove loop

    * Address comments

    * Fix build fail

    * Address comments

    * Remove unused imports

    * Fix method name

    * Split intermediate and leaf combine degrees

    * Add comments to StreamingMergeSortedGrouper

    * Add more comments and fix overflow

    * Address comments

    * ConcurrentGrouperTest cleanup

    * add thread number configuration for parallel combining

    * improve doc

    * address comments

    * fix build