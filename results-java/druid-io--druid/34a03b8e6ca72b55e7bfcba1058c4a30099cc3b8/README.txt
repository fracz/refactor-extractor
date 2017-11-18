commit 34a03b8e6ca72b55e7bfcba1058c4a30099cc3b8
Author: Gian Merlino <gianmerlino@gmail.com>
Date:   Fri Sep 1 09:35:13 2017 -0700

    SQL: EXPLAIN improvements. (#4733)

    * SQL: EXPLAIN improvements.

    - Include query JSON in explain output.
    - Fix a bug where semi-joins and nested groupBys were not fully explained.
    - Fix a bug where limits were not included in "select" query explanations.

    * Fix compile error.

    * Fix compile error.

    * Fix tests.