commit f6acfb08917946f15cb8de2fee786019124af212
Author: Ewen Cheslack-Postava <me@ewencp.org>
Date:   Fri Aug 14 16:00:51 2015 -0700

    KAFKA-2366; Initial patch for Copycat

    This is an initial patch implementing the basics of Copycat for KIP-26.

    The intent here is to start a review of the key pieces of the core API and get a reasonably functional, baseline, non-distributed implementation of Copycat in place to get things rolling. The current patch has a number of known issues that need to be addressed before a final version:

    * Some build-related issues. Specifically, requires some locally-installed dependencies (see below), ignores checkstyle for the runtime data library because it's lifted from Avro currently and likely won't last in its current form, and some Gradle task dependencies aren't quite right because I haven't gotten rid of the dependency on `core` (which should now be an easy patch since new consumer groups are in a much better state).
    * This patch currently depends on some Confluent trunk code because I prototyped with our Avro serializers w/ schema-registry support. We need to figure out what we want to provide as an example built-in set of serializers. Unlike core Kafka where we could ignore the issue, providing only ByteArray or String serializers, this is pretty central to how Copycat works.
    * This patch uses a hacked up version of Avro as its runtime data format. Not sure if we want to go through the entire API discussion just to get some basic code committed, so I filed KAFKA-2367 to handle that separately. The core connector APIs and the runtime data APIs are entirely orthogonal.
    * This patch needs some updates to get aligned with recent new consumer changes (specifically, I'm aware of the ConcurrentModificationException issue on exit). More generally, the new consumer is in flux but Copycat depends on it, so there are likely to be some negative interactions.
    * The layout feels a bit awkward to me right now because I ported it from a Maven layout. We don't have nearly the same level of granularity in Kafka currently (core and clients, plus the mostly ignored examples, log4j-appender, and a couple of contribs). We might want to reorganize, although keeping data+api separate from runtime and connector plugins is useful for minimizing dependencies.
    * There are a variety of other things (e.g., I'm not happy with the exception hierarchy/how they are currently handled, TopicPartition doesn't really need to be duplicated unless we want Copycat entirely isolated from the Kafka APIs, etc), but I expect those we'll cover in the review.

    Before commenting on the patch, it's probably worth reviewing https://issues.apache.org/jira/browse/KAFKA-2365 and https://issues.apache.org/jira/browse/KAFKA-2366 to get an idea of what I had in mind for a) what we ultimately want with all the Copycat patches and b) what we aim to cover in this initial patch. My hope is that we can use a WIP patch (after the current obvious deficiencies are addressed) while recognizing that we want to make iterative progress with a bunch of subsequent PRs.

    Author: Ewen Cheslack-Postava <me@ewencp.org>

    Reviewers: Ismael Juma, Gwen Shapira

    Closes #99 from ewencp/copycat and squashes the following commits:

    a3a47a6 [Ewen Cheslack-Postava] Simplify Copycat exceptions, make them a subclass of KafkaException.
    8c108b0 [Ewen Cheslack-Postava] Rename Coordinator to Herder to avoid confusion with the consumer coordinator.
    7bf8075 [Ewen Cheslack-Postava] Make Copycat CLI speific to standalone mode, clean up some config and get rid of config storage in standalone mode.
    656a003 [Ewen Cheslack-Postava] Clarify and expand the explanation of the Copycat Coordinator interface.
    c0e5fdc [Ewen Cheslack-Postava] Merge remote-tracking branch 'origin/trunk' into copycat
    0fa7a36 [Ewen Cheslack-Postava] Mark Copycat classes as unstable and reduce visibility of some classes where possible.
    d55d31e [Ewen Cheslack-Postava] Reorganize Copycat code to put it all under one top-level directory.
    b29cb2c [Ewen Cheslack-Postava] Merge remote-tracking branch 'origin/trunk' into copycat
    d713a21 [Ewen Cheslack-Postava] Address Gwen's review comments.
    6787a85 [Ewen Cheslack-Postava] Make Converter generic to match serializers since some serialization formats do not require a base class of Object; update many other classes to have generic key and value class type parameters to match this change.
    b194c73 [Ewen Cheslack-Postava] Split Copycat converter option into two options for key and value.
    0b5a1a0 [Ewen Cheslack-Postava] Normalize naming to use partition for both source and Kafka, adjusting naming in CopycatRecord classes to clearly differentiate.
    e345142 [Ewen Cheslack-Postava] Remove Copycat reflection utils, use existing Utils and ConfigDef functionality from clients package.
    be5c387 [Ewen Cheslack-Postava] Minor cleanup
    122423e [Ewen Cheslack-Postava] Style cleanup
    6ba87de [Ewen Cheslack-Postava] Remove most of the Avro-based mock runtime data API, only preserving enough schema functionality to support basic primitive types for an initial patch.
    4674d13 [Ewen Cheslack-Postava] Address review comments, clean up some code styling.
    25b5739 [Ewen Cheslack-Postava] Fix sink task offset commit concurrency issue by moving it to the worker thread and waking up the consumer to ensure it exits promptly.
    0aefe21 [Ewen Cheslack-Postava] Add log4j settings for Copycat.
    220e42d [Ewen Cheslack-Postava] Replace Avro serializer with JSON serializer.
    1243a7c [Ewen Cheslack-Postava] Merge remote-tracking branch 'origin/trunk' into copycat
    5a618c6 [Ewen Cheslack-Postava] Remove offset serializers, instead reusing the existing serializers and removing schema projection support.
    e849e10 [Ewen Cheslack-Postava] Remove duplicated TopicPartition implementation.
    dec1379 [Ewen Cheslack-Postava] Switch to using new consumer coordinator instead of manually assigning partitions. Remove dependency of copycat-runtime on core.
    4a9b4f3 [Ewen Cheslack-Postava] Add some helpful Copycat-specific build and test targets that cover all Copycat packages.
    31cd1ca [Ewen Cheslack-Postava] Add CLI tools for Copycat.
    e14942c [Ewen Cheslack-Postava] Add Copycat file connector.
    0233456 [Ewen Cheslack-Postava] Add copycat-avro and copycat-runtime
    11981d2 [Ewen Cheslack-Postava] Add copycat-data and copycat-api