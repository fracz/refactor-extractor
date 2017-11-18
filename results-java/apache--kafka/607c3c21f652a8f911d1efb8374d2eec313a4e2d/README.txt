commit 607c3c21f652a8f911d1efb8374d2eec313a4e2d
Author: huxihx <huxi_2b@hotmail.com>
Date:   Fri Aug 25 10:38:15 2017 -0700

    KAFKA-5755; KafkaProducer should be refactored to use LogContext

    With LogContext, each producer log item is automatically prefixed with client id and transactional id.

    Author: huxihx <huxi_2b@hotmail.com>

    Reviewers: Jason Gustafson <jason@confluent.io>

    Closes #3703 from huxihx/KAFKA-5755