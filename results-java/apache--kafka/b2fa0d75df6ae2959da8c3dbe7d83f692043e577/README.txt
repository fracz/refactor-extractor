commit b2fa0d75df6ae2959da8c3dbe7d83f692043e577
Author: Matthias J. Sax <matthias@confluent.io>
Date:   Mon Oct 24 13:44:27 2016 -0700

    KAFKA-4331: Kafka Streams resetter is slow because it joins the same group for each topic

    - reworked to use a sinlge KafkaConsumer and subscribe only once

    Author: Matthias J. Sax <matthias@confluent.io>

    Reviewers: Guozhang Wang <wangguoz@gmail.com>

    Closes #2049 from mjsax/improveResetTool