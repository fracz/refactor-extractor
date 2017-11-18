commit a459db68b6ff43b285475f3a6cb8381404b4a45d
Author: Jihoon Son <jihoonson@apache.org>
Date:   Wed Feb 15 05:55:54 2017 +0900

    Fine grained buffer management for groupby (#3863)

    * Fine-grained buffer management for group by queries

    * Remove maxQueryCount from GroupByRules

    * Fix code style

    * Merge master

    * Fix compilation failure

    * Address comments

    * Address comments

    - Revert Sequence
    - Add isInitialized() to Grouper
    - Initialize the grouper in RowBasedGrouperHelper.Accumulator
    - Simple refactoring RowBasedGrouperHelper.Accumulator
    - Add tests for checking the number of used merge buffers
    - Improve docs

    * Revert unnecessary changes

    * change to visible to testing

    * fix misspelling