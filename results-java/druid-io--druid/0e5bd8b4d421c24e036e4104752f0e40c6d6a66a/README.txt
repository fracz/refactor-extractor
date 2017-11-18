commit 0e5bd8b4d421c24e036e4104752f0e40c6d6a66a
Author: Jonathan Wei <jon-wei@users.noreply.github.com>
Date:   Wed Dec 21 19:11:37 2016 -0800

    Add dimension type-based interface for query processing (#3570)

    * Add dimension type-based interface for query processing

    * PR comment changes

    * Address PR comments

    * Use getters for QueryDimensionInfo

    * Split DimensionQueryHelper into base interface and query-specific interfaces

    * Treat empty rows as nulls in v2 groupby

    * Reduce boxing in SearchQueryRunner

    * Add GroupBy empty row handling to MultiValuedDimensionTest

    * Address PR comments

    * PR comments and refactoring

    * More PR comments

    * PR comments