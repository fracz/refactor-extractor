commit dfa9cdc98270f8c8d9bbc4f3db11f9c0aa6d75da
Author: Jihoon Son <jihoonson@apache.org>
Date:   Thu Oct 12 15:16:31 2017 +0900

    Prioritized locking (#4550)

    * Implementation of prioritized locking

    * Fix build failure

    * Fix tc fail

    * Fix typos

    * Fix IndexTaskTest

    * Addressed comments

    * Fix test

    * Fix spacing

    * Fix build error

    * Fix build error

    * Add lock status

    * Cleanup suspicious method

    * Add nullables

    *  add doInCriticalSection to TaskLockBox and revert return type of task actions

    * fix build

    * refactor CriticalAction

    * make replaceLock transactional

    * fix formatting

    * fix javadoc

    * fix build