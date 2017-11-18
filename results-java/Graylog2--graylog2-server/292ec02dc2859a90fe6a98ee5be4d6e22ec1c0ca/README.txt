commit 292ec02dc2859a90fe6a98ee5be4d6e22ec1c0ca
Author: Bernd Ahlers <bernd@tuneafish.de>
Date:   Wed Nov 9 16:34:51 2016 +0100

    Route messages to different index sets (#2976)

    To be able to write a message to different indices/index-sets we have to
    map streams to index sets and handle that when writing to Elasticsearch.

    - Track matching index sets in the `Message` object to we don't have to
      iterate over all streams when doing the mapping. This assumes that
      usually there will be fewer index-sets than streams for a message.

    - Add `Stream#getIndexSets()` and add the legacy default index-set to
      each stream. This needs to be changed to load the index-sets for a
      stream from the database.

    - Adjust the `BlockingBatchedESOutput` to write one message into all
      index-sets for that message. Depending on the number of index-sets for
      a message, we are actually writing multiple messages for each message.
      This can slow down indexing because more work has to be done.
      The batching behaviour doesn't change because we are still flushing
      after the configured batch size.

    - The `Messages#bulkIndex()` method now takes a small `IndexAndMessage`
      object to decide into which index a message should be indexed.

    The reasoning behind putting the index-sets into the message instead of
    looking them up later is that we just have to iterate over index sets
    instead of iterating over streams and looking up index-sets from them.

    Since we are rebuilding the stream router in a background thread when
    stream (and soon index-set) configuration changes, the expensive work is
    done outside of the hot code path.

    There are probably several improvements that can be implemented to make
    this more efficient. This is a first naive version that makes it work
    with minimal changes.