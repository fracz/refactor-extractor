commit e6b343302f3208f7f6e0099fe2a7132ef9eaaafb
Author: Geoff Anderson <geoff@confluent.io>
Date:   Tue Oct 27 15:23:47 2015 -0700

    KAFKA-1888: rolling upgrade test

    ewencp gwenshap
    This needs some refactoring to avoid the duplicated code between replication test and upgrade test, but in shape for initial feedback.

    I'm interested in feedback on the added `KafkaConfig` class and `kafka_props` file. This addition makes it:
    - easier to attach different configs to different nodes (e.g. during broker upgrade process)
    - easier to reason about the configuration of a particular node

    Notes:
    - in the default values in the KafkaConfig class, I removed many properties which were in kafka.properties before. This is because most of those properties were set to what is already the default value.
    - when running non-trunk VerifiableProducer, I append the trunk tools jar to the classpath, and run it with the non-trunk kafka-run-class.sh script

    Author: Geoff Anderson <geoff@confluent.io>

    Reviewers: Dong Lin, Ewen Cheslack-Postava

    Closes #229 from granders/KAFKA-1888-upgrade-test