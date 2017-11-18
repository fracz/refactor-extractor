commit cc2026007832790d12e70016dbd1a7ee00333ff0
Author: Jihoon Son <jihoonson@apache.org>
Date:   Tue Jul 11 14:35:36 2017 +0900

    Early publishing segments in the middle of data ingestion (#4238)

    * Early publishing segments in the middle of data ingestion

    * Remove unnecessary logs

    * Address comments

    * Refactoring the patch according to #4292 and address comments

    * Set the total shard number of NumberedShardSpec to 0

    * refactoring

    * Address comments

    * Fix tests

    * Address comments

    * Fix sync problem of committer and retry push only

    * Fix doc

    * Fix build failure

    * Address comments

    * Fix compilation failure

    * Fix transient test failure