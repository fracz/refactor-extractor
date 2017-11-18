commit ac84a3e011b3978652148668a9eb3b0fd95cc473
Author: Gian Merlino <gianmerlino@gmail.com>
Date:   Wed Jan 25 18:39:26 2017 -0800

    SQL: Add resolution parameter, fix filtering bug with APPROX_QUANTILE (#3868)

    * SQL: Add resolution parameter to quantile agg, rename to APPROX_QUANTILE.

    * Fix bug with re-use of filtered approximate histogram aggregators.

    Also add APPROX_QUANTILE tests for filtering and running on complex columns.
    Includes some slight refactoring to allow tests to make DruidTables that
    include complex columns.

    * Remove unused import