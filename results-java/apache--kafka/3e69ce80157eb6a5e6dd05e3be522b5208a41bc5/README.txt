commit 3e69ce80157eb6a5e6dd05e3be522b5208a41bc5
Author: Damian Guy <damian.guy@gmail.com>
Date:   Fri Aug 11 12:14:01 2017 +0100

    KAFKA-5702; extract refactor StreamThread

    Extracted `TaskManager` to handle all task related activities.
    Make `StandbyTaskCreator`, `TaskCreator`, and `RebalanceListener` static classes so they must define their dependencies and can be testing independently of `StreamThread`
    Added interfaces between `StreamPartitionAssignor` & `StreamThread` to reduce coupling.

    Author: Damian Guy <damian.guy@gmail.com>

    Reviewers: Bill Bejeck <bill@confluent.io>, Guozhang Wang <wangguoz@gmail.com>, Eno Thereska <eno.thereska@gmail.com>

    Closes #3624 from dguy/stream-thread-refactor