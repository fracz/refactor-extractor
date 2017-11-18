commit a649b7a3d40eae5fb7cbbe6b37988a002b6a1ed2
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Tue Oct 1 21:14:31 2013 -0400

    Monster getKeys refactoring and testing commit

    The getKeys(KeyRangeQuery keyQuery, StoreTransaction txh) method in
    the Astyanax and Thrift stores seemed to be untested and broken.  This
    is the getKeys method that takes both a column range and key range.
    Its only supported when the backend stores keys in byte order (BOP, in
    this case).

    The Thrift adapter had these bugs:

    * For Cassandra versions 1.2.4 and earlier,
      https://issues.apache.org/jira/browse/CASSANDRA-5566 makes the
      client send broken slice tokens when BOP is in effect.  The server
      can't parse the tokens, logging an error and triggering an exception
      client-side.  Slicing by key is not affected -- only by tokens.
      This has a pretty simple workaround, and it goes away in 1.2.5 and
      up.  I actually already verified that it's fixed on 1.2.10, but then
      I had to revert to 1.2.2 when I discovered that Astyanax has a hard
      incompatibility (trying to call a nonexistent constructor) with
      1.2.10.

    * The ordered getKeys checked for `partitioner instanceof
      OrderPreservingPartitioner` and threw an exception if false.  But
      this is always false with BOP.  BOP doesn't inherit from OPP and
      never has AFAIK (= moderate certainty back to about 0.7).  Changed
      OPP in the instanceof check to AbstractByteOrderedPartitioner.  I
      have an extremely strong sense of déjà vu about this bug, but I
      don't know where in the revision history I might have previously
      fixed it, if at all.

    * The last key was returned by the ordered getKeys method.  Judging
      from KeyRangeQuery and the tests for this method, I think the last
      key is supposed to be considered exclusive.

    The Astyanax adapter just had the last bug in the list above.  I fixed
    all of the above (I think).

    I fixed the first bullet point about junk BOP tokens in a
    forward-compatible way.  When we go to 1.2.5 and beyond, the
    workaround should quietly turn itself off at runtime.  Then we can
    remove it as soon as we're ready to drop compatibility with prior
    versions.  I also added forward-compatibility to
    CassandraThriftStorageManager's token handling; it wasn't buggy on our
    current Cassandra version, but it would have failed when we eventually
    upgrade to 1.2.5 and beyond.

    I refactored Cassandra tests and the way Maven runs them.  By default,
    all Cassandra tests now run twice with Maven: once under BOP and once
    under RP.

    Test classes or methods can be marked as eligible for only BOP or RP
    by annotating them with

     @Category({ UnorderedKeyStoreTests.class })

    or

     @Category({ OrderedKeyStoreTests.class })

    There is also an annotation which explicitly declares the subject
    compatible with either type of store, but it is redundant, because
    that is the default behavior under the current Maven config:

     @Category({ KeyOrderAgnosticTests.class })

    All of the annotation argument classes are in the package
    com.thinkaurelius.titan.testcategory in titan-test.

    Eclipse's native JUnit runner appears to have no way to honor the new
    JUnit @Category annotation, so it will blithely try to run all tests
    regardless of their categories using the default config, which I've
    set to BOP.  When Eclipse runs an BOP-only test with a RP config, it
    fails.  I haven't found a clean way to workaround that yet.  We could
    make all the tests interrogate StoreFeatures to see what's possible,
    but that will add some boilerplate.  It would be worth the boilerplate
    to get the tests to "just work" in Eclipse though.

    Finally, both ExpectedValueCheckingStore and
    BufferedKeyColumnValueStore had bugs in both of their getKeys methods:
    the one that takes a SliceQuery and the other that takes a
    KeyRangeQuery.  They weren't unwrapping their transactions when
    interacting with the wrapped store.

    I also finished removing Rexster classes, test classes, and Maven
    dependencies.