commit ede7cf9eef6518f818dbd477ad167913acc92aa4
Author: Peter Cunningham <pcunningham@spotxchange.com>
Date:   Wed Aug 9 18:47:46 2017 +0100

    Added support for where clauses to JDBC lookups. (#4643)

    * Added support for where clauses to filter lookup values on ingestion.

    Added a filter field to the JDBC lookups that is used to generate a
    where clause so that only rows matching the filter value will be
    brought into Druid. Example being filter="SOMECOLUMN=1"

    * Required changes based on code review.

    * Required changes based on code review.

    * Added support for where clauses to filter lookup values on ingestion.

    Added a filter field to the JDBC lookups that is used to generate a
    where clause so that only rows matching the filter value will be
    brought into Druid. Example being filter="SOMECOLUMN=1"

    * Updates based on code review, mainly formatting and small refactor of
    the buildLookupQuery method.

    * Fixed broken buildLookupQuery method

    * Removed empty line.

    * Updates per review comments