commit e19c37e5914b1fa64befdfdf24edbf2415f8cdb3
Author: Damian Guy <damian.guy@gmail.com>
Date:   Mon Aug 14 10:02:32 2017 +0100

    KAFKA-5673; refactor KeyValueStore hierarchy to make MeteredKeyValueStore outermost

    refactor StateStoreSuppliers such that a `MeteredKeyValueStore`  is the outermost store.

    Author: Damian Guy <damian.guy@gmail.com>

    Reviewers: Eno Thereska <eno.thereska@gmail.com>, Guozhang Wang <wangguoz@gmail.com>

    Closes #3592 from dguy/key-value-store-refactor