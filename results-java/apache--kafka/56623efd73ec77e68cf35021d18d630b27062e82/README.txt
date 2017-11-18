commit 56623efd73ec77e68cf35021d18d630b27062e82
Author: Randall Hauch <rhauch@gmail.com>
Date:   Thu May 18 16:02:29 2017 -0700

    KAFKA-4667: Connect uses AdminClient to create internal topics when needed (KIP-154)

    The backing store for offsets, status, and configs now attempts to use the new AdminClient to look up the internal topics and create them if they don’t yet exist. If the necessary APIs are not available in the connected broker, the stores fall back to the old behavior of relying upon auto-created topics. Kafka Connect requires a minimum of Apache Kafka 0.10.0.1-cp1, and the AdminClient can work with all versions since 0.10.0.0.

    All three of Connect’s internal topics are created as compacted topics, and new distributed worker configuration properties control the replication factor for all three topics and the number of partitions for the offsets and status topics; the config topic requires a single partition and does not allow it to be set via configuration. All of these new configuration properties have sensible defaults, meaning users can upgrade without having to change any of the existing configurations. In most situations, existing Connect deployments will have already created the storage topics prior to upgrading.

    The replication factor defaults to 3, so anyone running Kafka clusters with fewer nodes than 3 will receive an error unless they explicitly set the replication factor for the three internal topics. This is actually desired behavior, since it signals the users that they should be aware they are not using sufficient replication for production use.

    The integration tests use a cluster with a single broker, so they were changed to explicitly specify a replication factor of 1 and a single partition.

    The `KafkaAdminClientTest` was refactored to extract a utility for setting up a `KafkaAdminClient` with a `MockClient` for unit tests.

    Author: Randall Hauch <rhauch@gmail.com>

    Reviewers: Ewen Cheslack-Postava <ewen@confluent.io>

    Closes #2984 from rhauch/kafka-4667