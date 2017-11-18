commit 5aedde5b29cc5a1adea4c65d5c44fb657f162eab
Author: Guozhang Wang <wangguoz@gmail.com>
Date:   Thu May 12 11:12:17 2016 +0100

    MINOR: Change type of StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to List

    This is an improved version of https://github.com/apache/kafka/pull/1374, where we include a unit test.

    /cc ijuma and guozhangwang

    Author: Guozhang Wang <wangguoz@gmail.com>
    Author: Michael G. Noll <michael@confluent.io>

    Reviewers: Michael G. Noll <michael@confluent.io>, Ismael Juma <ismael@juma.me.uk>

    Closes #1377 from miguno/streamsconfig-multiple-bootstrap-servers