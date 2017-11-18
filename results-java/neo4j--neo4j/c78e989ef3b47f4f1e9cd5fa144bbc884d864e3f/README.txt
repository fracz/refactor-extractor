commit c78e989ef3b47f4f1e9cd5fa144bbc884d864e3f
Author: lutovich <konstantin.lutovich@neotechnology.com>
Date:   Tue Jan 26 01:04:51 2016 +0100

    Use NRTCachingDirectory over FSDirectory

    NRTCachingDirectory is a Directory implementation that wraps a RAMDirectory
    over the given delegate persistent Directory. The in-memory directory caches
    small Lucene segments and lowers merge cost for them.

    This change makes cost of SearcherManager#maybeRefresh after insertion of new
    documents lower and improves write performance.

    co-author: @MishaDemianenko