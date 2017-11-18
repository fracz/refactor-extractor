commit c5f68e746a7c5d596ecc3e56db85ad229f1077e8
Author: tinwelint <mattias@neotechnology.com>
Date:   Fri Nov 18 13:02:14 2016 +0100

    Initial version of a NativeLabelScanStore

    built using our GB+Tree as storage. Storage structure-wise it's very similar
    to the current Lucene label scan store in that nodes are grouped into small
    bit sets of 64 as the unit of storage.

    Initial benchmarks show big write performance improvements (within and up to
    an order of magnitude, even for heavily concurrent write load). Read performance
    is equal to or better than that of Lucene label scan store.

    Disk space-wise it occupies a bit more than Lucene lss, within 2x.
    Memory-wise it's much less because there's no caching of the index,
    only the page cache itself.
    Cost of opening a native lss is small and constant compared to lucene lss,
    or rather Lucene in general where cost of opening index is a function of the size
    of the index. As an example it could take a minute to open a Lucene lss on
    a store with 10's of billions of labelled nodes, sub-second with the native lss.
    Additionally there's no background processing or file-merging on native lss.

    Currently this lss implementation is disabled by default, as implmentation
    gets more complete over time this will be the default lss for Neo4j builds.