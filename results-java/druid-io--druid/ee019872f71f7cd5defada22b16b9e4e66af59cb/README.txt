commit ee019872f71f7cd5defada22b16b9e4e66af59cb
Author: Charles Allen <charlesallen@Charless-MacBook-Pro.local>
Date:   Tue Nov 4 18:43:55 2014 -0800

    TopN performance improvements

    Re-factor scanAndAggregate in PooledTopN

    * Loops are now a little bit tighter when looping over aggregates. This will hopefully assist in loop execution optimization.
    * Pre-calculated the aggregate offsets instead of shifting them during runtime.
    * Cursor loop could use some TLC, but would require a massive refactoring on how TopN queries are executed.
      * Any potential modifications to query workflow need to account for Stream vs Batch data, and that not all data will be array backed that comes in.

    Change data storage type in TopNNumericResultBuilder.

      * Use PriorityQueue to store
      * Checks to see if should even bother adding to Queue before adding.
      * Re-orders Queue on build() call.
      * Ideally the order would be directly preserved on build(), but this is a close second.

    Updates to CompressedObjectStrategy to support more compression types

     * Compression types are not yet dynamically configurable.
     * Added a benchmarking system for topN to test the compression
     * Updated pom.xml to include junit benchmarking
     * added an Uncompressed option