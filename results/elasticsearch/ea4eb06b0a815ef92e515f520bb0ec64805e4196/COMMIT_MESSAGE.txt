commit ea4eb06b0a815ef92e515f520bb0ec64805e4196
Author: Nik Everett <nik9000@gmail.com>
Date:   Fri Feb 3 08:00:57 2017 -0500

    Test: Make update-by-query test more resilient

    `UpdateByQueryWhileModifyingTests#testUpdateWhileReindexing`
    runs update-by-query and concurrently updates, asserting that
    the update-by-query never reverts any changes made by the update.
    It is a smoke test for concurrent updates.

    Now, it expects to hit a certain number of version conflicts
    during the updates. This is normal as it is racing the
    update-by-query. We have a maximum number of failures we
    expect (10) and I'd never seen us come close until
    https://elasticsearch-ci.elastic.co/job/elastic+elasticsearch+5.x+multijob-unix-compatibility/os=sles/495/console

    This bumps the max failures from 10 to 50 and improves
    logging a bit. If we continue to see this failure then we have
    some other issue.

    Closes #22938