commit b128446e1c03b0233322f7716e2534936858ff75
Author: Raghav Sethi <raghavsethi@fb.com>
Date:   Mon Nov 23 18:06:08 2015 -0800

    Add optimizer to pushdown writes through UNION ALL

    When processing queries that created tables from UNION ALLs of other
    tables, Presto would collect all results from the UNION ALL into a
    single node prior to writing. Added an optimizer that pushes the
    TableWriter nodes through the union. This will improve performance by
    allowing workers to write to the new table in parallel.