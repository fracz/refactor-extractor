commit 5ec242bc149ba1e9ac8ce9499bd8fe17ef0ef75b
Author: Tyron Madlener <tyronx@gmail.com>
Date:   Tue Aug 2 11:25:26 2011 +0300

    Small improvements:
    - Byte value also for query_cache_limit
    - In Status and Server variables, '_' and ' ' can be used interchangably, but only worked for the first occurence
    - For analysing queries from slow query log, database name is passed on and selected prior to analyzing the query
    - Timespan format also for Uptime