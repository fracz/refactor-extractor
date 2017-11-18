commit 98c190442d4eca4edc61f1cf3717495fe380b88c
Author: Adrian Cole <acole@pivotal.io>
Date:   Mon Mar 7 20:39:32 2016 +0800

    Adds CassandraSpanStore including zipkin-server support

    This is a port of `CassandraSpanStore` from zipkin-scala, except notably
    not including the "slice query" infrastructure.

    Note: While `CassandraSpanStore.accept` doesn't return a `Future`, it is
    still asynchronous inside.

    `zipkin-server` has been extended to optionally support Cassandra using
    the same environment variables as zipkin-scala. For example, you can
    start a process using cassandra as simply as setting the `STORAGE_TYPE`:

    ```bash
    STORAGE_TYPE=cassandra ./mvnw -pl zipkin-server spring-boot:run
    ```

    This uses the `Repository` class from `zipkin-cassandra-core`, which
    internally uses DataStax Java Driver 2.1. It is likely that we'll
    refactor out this dependency, for a couple reasons. One is that DataStax
    Java Driver 3 is current. Another is that `Repository` was designed for
    "slice query" infrastructure that doesn't exist in this project: there
    are places where logic would be simpler to address in raw CQL.

    This was tested using zipkin-scala's integration tests, local integration
    tests, running the server with spring boot, and processing data with
    `zipkin-dependencies-spark`.