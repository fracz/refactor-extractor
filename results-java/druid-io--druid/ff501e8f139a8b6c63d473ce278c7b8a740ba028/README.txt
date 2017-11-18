commit ff501e8f139a8b6c63d473ce278c7b8a740ba028
Author: Fokko Driesprong <fokko@driesprong.frl>
Date:   Thu Jun 22 22:56:08 2017 +0200

    Add Date support to the parquet reader (#4423)

    * Add Date support to the parquet reader

    Add support for the Date logical type. Currently this is not supported. Since the parquet
    date is number of days since epoch gets interpreted as seconds since epoch, it will fails
    on indexing the data because it will not map to the appriopriate bucket.

    * Cleaned up code and tests

    Got rid of unused json files in the examples, cleaned up the tests by
    using try-with-resources. Now get the filenames from the json file
    instead of hard coding them and integrated general improvements from
    the feedback provided by leventov.

    * Got rid of the caching

    Remove the caching of the logical type of the time dimension column
    and cleaned up the code a bit.