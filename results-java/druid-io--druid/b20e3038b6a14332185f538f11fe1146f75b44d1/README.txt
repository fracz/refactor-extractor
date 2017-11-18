commit b20e3038b6a14332185f538f11fe1146f75b44d1
Author: Gian Merlino <gianmerlino@gmail.com>
Date:   Tue Oct 10 12:44:05 2017 -0700

    SQL: Upgrade to Calcite 1.14.0, some refactoring of internals. (#4889)

    * SQL: Upgrade to Calcite 1.14.0, some refactoring of internals.

    This brings benefits:
    - Ability to do GROUP BY and ORDER BY with ordinals.
    - Ability to support IN filters beyond 19 elements (fixes #4203).

    Some refactoring of druid-sql internals:
    - Builtin aggregators and operators are implemented as SqlAggregators
      and SqlOperatorConversions rather being special cases. This simplifies
      the Expressions and GroupByRules code, which were becoming complex.
    - SqlAggregator implementations are no longer responsible for filtering.

    Added new functions:
    - Expressions: strpos.
    - SQL: TRUNCATE, TRUNC, LENGTH, CHAR_LENGTH, STRLEN, STRPOS, SUBSTR,
      and DATE_TRUNC.

    * Add missing @Override annotation.

    * Adjustments for forbidden APIs.

    * Adjustments for forbidden APIs.

    * Disable GROUP BY alias.

    * Doc reword.