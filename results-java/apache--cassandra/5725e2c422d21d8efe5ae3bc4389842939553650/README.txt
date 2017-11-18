commit 5725e2c422d21d8efe5ae3bc4389842939553650
Author: Corentin Chary <c.chary@criteo.com>
Date:   Mon Jan 9 12:06:56 2017 -0500

    Coalescing strategies improvements CASSANDRA-13090

    With the previous code TIMEHORIZON and MOVINGAVERAGE
    coalesing strategy would wait even when the backlog
    still contains data which would make it grow even more.

    Also:
    - cleanups parkLoop()
    - add otc_coalescing_max_coalesced_messages
    - add otc_coalescing_enough_coalesced_messages
    - add other otc_* settings to cassandra.yaml

    patch by Corentin Chary <c.chary@criteo.com> reviewed by Ariel Weisberg <aweisberg@apple.com> for CASSANDRA-13090