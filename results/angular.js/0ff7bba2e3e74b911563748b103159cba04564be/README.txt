commit 0ff7bba2e3e74b911563748b103159cba04564be
Author: Martin Staffa <mjstaffa@googlemail.com>
Date:   Thu Aug 13 15:30:50 2015 +0200

    test(select): clean up and improve the option directive tests

    - add tests to ensure options with interpolated text are added / updated
    - refactor tests for interpolated option values to use the
    standard compile helper defined in the spec file.
    - rephrase some test descriptions for clarity

    Closes #12580