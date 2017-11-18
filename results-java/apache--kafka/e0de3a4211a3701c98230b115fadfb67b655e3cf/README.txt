commit e0de3a4211a3701c98230b115fadfb67b655e3cf
Author: Damian Guy <damian.guy@gmail.com>
Date:   Fri Jan 6 10:12:30 2017 -0800

    KAFKA-3452: Support session windows

    Add support for SessionWindows based on design detailed in https://cwiki.apache.org/confluence/display/KAFKA/KIP-94+Session+Windows.
    This includes refactoring of the RocksDBWindowStore such that functionality common with the RocksDBSessionStore isn't duplicated.

    Author: Damian Guy <damian.guy@gmail.com>

    Reviewers: Eno Thereska <eno.thereska@gmail.com>, Matthias J. Sax <matthias@confluent.io>, Guozhang Wang <wangguoz@gmail.com>

    Closes #2166 from dguy/kafka-3452-session-merge