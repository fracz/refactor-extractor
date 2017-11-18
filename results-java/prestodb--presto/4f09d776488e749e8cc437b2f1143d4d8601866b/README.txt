commit 4f09d776488e749e8cc437b2f1143d4d8601866b
Author: Rebecca Schlussel <rebecca.schlussel@teradata.com>
Date:   Tue Mar 7 13:14:30 2017 -0500

    Add rewrite to push aggregations below outer joins

    When an aggregation is on top of an outer join and groups by all columns
    in the outer table, and the outer rows are guaranteed to be distinct,
    the
    aggregation can be pushed to the inner side of the join and group on the
    join keys.  This will improve performance of correlated scalar
    subqueries, which get rewritten to an aggregation above a left join

    Example:

    SELECT *
    FROM nation n1
    WHERE (n1.nationkey > (
          SELECT avg(nationkey)
          FROM nation n2
          WHERE n1.regionkey=n2.regionkey))

    Gets rewritten by the scalar subquery rewrite to:
    - Filter ("nationkey" > "avg")
        - Aggregate(Group by: all columns from the left talbe, aggregation:
          avg("n2.nationkey"))
             - LeftJoin("regionkey" = "regionkey")
                 - AssignUniqueId (nation)
                      - Tablescan (nation)
                  - Tablescan (nation)

    This rewrite pushes down the aggregation. In doing so, it also allows
    the AssignUniqueIdNode to get pruned as unused. When the aggregation is
    pushed down, we still need to perform aggregations on the null values
    that come from absent values in an outer join. We add a cross join with
    a row of aggregations on null literals, and coalesce the aggregation
    that
    results from the left outer join with the result of the aggregation over
    nulls.

    The result is:
    - Filter ("nationkey" > "avg")
             - project(regionkey, coalesce("avg", "avg_over_null")
                - CrossJoin
                    - LeftJoin("regionkey" = "regionkey")
                         - Tablescan (nation)
                         - Aggregate(Group by: regionkey, aggregation:
                           avg(nationkey))
                              - Tablescan (nation)
                - Aggregate
                  avg(null_literal)
                    - Values (null_literal)