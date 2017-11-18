commit 36d4469326fe20c3f0657315321e6ad515530a3e
Author: Ewen Cheslack-Postava <me@ewencp.org>
Date:   Tue Oct 13 10:23:21 2015 -0700

    KAFKA-2372: Add Kafka-backed storage of Copycat configs.

    This also adds some other needed infrastructure for distributed Copycat, most
    importantly the DistributedHerder, and refactors some code for handling
    Kafka-backed logs into KafkaBasedLog since this is shared betweeen offset and
    config storage.

    Author: Ewen Cheslack-Postava <me@ewencp.org>

    Reviewers: Gwen Shapira, James Cheng

    Closes #241 from ewencp/kafka-2372-copycat-distributed-config