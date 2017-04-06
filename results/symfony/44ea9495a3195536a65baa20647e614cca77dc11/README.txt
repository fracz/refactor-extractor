commit 44ea9495a3195536a65baa20647e614cca77dc11
Merge: 48af0ba fab1b5a
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 18 10:36:16 2012 +0200

    merged branch eriksencosta/locale-fixes-2.0 (PR #3955)

    Commits
    -------

    fab1b5a [Locale] changed inequality operator to strict checking and updated some assertions
    09d30d3 [Locale] refactored some code
    e4cbbf3 [Locale] fixed StubNumberFormatter::format() to behave like the NumberFormatter::parse() regarding to error flagging
    f16ff89 [Locale] fixed StubNumberFormatter::parse() to behave like the NumberFormatter::parse() regarding to error flagging
    0a60664 [Locale] updated StubIntlDateFormatter::format() exception message when timestamp argument is an array for PHP >= 5.3.4
    e4769d9 [Locale] reordered test methods
    312a5a4 [Locale] fixed StubIntlDateFormatter::format() to set the right error for PHP >= 5.3.4 and to behave like the intl when formatting successfully

    Discussion
    ----------

    [2.0][Locale] some fixes

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Todo: -

    Fixed some inconsistencies between the stub and the intl implementation:

     - `StubIntlDateFormatter::format()` to set the right error for PHP >= 5.3.4 and to behave like the intl when formatting successfully
     - updated `StubIntlDateFormatter::format()` exception message when timestamp argument is an array for PHP >= 5.3.4
     - `StubNumberFormatter::parse()` to behave like the NumberFormatter::parse() regarding to error flagging
     - `StubNumberFormatter::format()` to behave like the NumberFormatter::parse() regarding to error flagging