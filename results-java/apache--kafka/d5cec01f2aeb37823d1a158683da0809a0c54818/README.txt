commit d5cec01f2aeb37823d1a158683da0809a0c54818
Author: Magnus Edenhill <magnus@edenhill.se>
Date:   Tue Mar 21 22:24:17 2017 -0700

    MINOR: Pluggable verifiable clients

    This adds support for pluggable VerifiableConsumer and VerifiableProducers test client implementations
    allowing third-party clients to be used in-place of the Java client in kafkatests.

    A new VerifiableClientMixin class is added and the standard Java Verifiable{Producer,Consumer}
    classes have been changed to use it.

    While third-party client drivers may be implemented with a complete class based on the Mixin, a simpler
    alternative which requries no kafkatest class implementation is available through the VerifiableClientApp class that uses ducktape's global param to specify the client app to use (passed to ducktape through the `--globals <json>` command line argument).

    Some existing kafkatest clients for reference:
    Go: https://github.com/confluentinc/confluent-kafka-go/tree/master/kafkatest
    Python: https://github.com/confluentinc/confluent-kafka-python/tree/master/confluent_kafka/kafkatest
    C++: https://github.com/edenhill/librdkafka/blob/0.9.2.x/examples/kafkatest_verifiable_client.cpp
    C#/.NET: https://github.com/confluentinc/confluent-kafka-dotnet/tree/master/test/Confluent.Kafka.VerifiableClient

    This PR also contains documentation for the simplex JSON-based verifiable\* client protocol.

    There are also some minor improvements that makes troubleshooting failing client tests easier.

    Author: Magnus Edenhill <magnus@edenhill.se>

    Reviewers: Ewen Cheslack-Postava <ewen@confluent.io>

    Closes #2048 from edenhill/pluggable_verifiable_clients