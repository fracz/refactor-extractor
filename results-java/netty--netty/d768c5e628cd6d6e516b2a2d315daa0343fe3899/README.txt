commit d768c5e628cd6d6e516b2a2d315daa0343fe3899
Author: Nikolay Fedorovskikh <fenik17@gmail.com>
Date:   Mon May 15 02:07:16 2017 +0500

    MessageFormatter improvements

    Motivation:

    `FormattingTuple.getArgArray()` is never used.
    In the `MessageFormatter` it is possible to make
    some improvements, e.g. replace `StringBuffer`
    with `StringBuilder`, avoid redundant allocations, etc.

    Modifications:

    - Remove `argArray` field from the `FormattingTuple`.
    - In `MessageFormatter`:
      - replace `StringBuffer` with `StringBuilder`,
      - replace `HashMap` with `HashSet` and make it lazy initialized.
      - avoid redundant allocations (`substring()`, etc.)
      - use appropriate StringBuilder's methods for the some `Number` values.
    - Porting unit tests from `slf4j`.

    Result:

    Less GC load on logging with internal `MessageFormatter`.