commit 8faedcbd7c8be62d0e3b75409792ca29d4380a25
Author: Carsten Brandt <mail@cebe.cc>
Date:   Wed Jan 4 02:58:49 2017 +0100

    Refactored UrlManagerTest to cover more use cases

    UrlManager is a complex class with a large bunch of options that needs
    to be tested
    and we had a few regressions in the past even though there are already a
    lot of tests
    covering it. Test coverage measured by lines does not help us determine
    how good it is tested,
    we need test coverage for every major path through the code.

    I have refactored the UrlManager tests to reflect the different options
    and cases to
    have a better overview of which cases are covered and which are not.

    UrlManager has two main operation modes:

    - "default" url format, which is the simple case. These are covered by
      methods in `UrlManagerTest`.

    - "pretty" url format. This is the complex case, which involves UrlRules
      and url parsing.
      I have created two separate classes for this case:
      Url creation for "pretty" url format is covered by
    `UrlManagerCreateUrlTest`.
      Url parsing for "pretty" url format is covered by
    `UrlManagerParseUrlTest`.

    Each of the test classes have a `getUrlManager` method that creates a
    UrlManager instance
    with a specific configuration and certain variations in options.
    It is also tested that options that are not relevant in a certain
    operation mode have no effect on the result.

    To make sure to not remove tests that have existed before, here is a map
    of where code has been moved.
    The following test methods existed in the [old test
    class](https://github.com/yiisoft/yii2/blob/4187718c148610b02fc973a19be53600e6230604/tests/framework/web/UrlManagerTest.php):

    - `testCreateUrl()` split between UrlManagerTest and
      UrlManagerCreateUrlTest variations should all be covered by
    `variationsProvider()`.
    - `testCreateUrlWithNullParams()` covered by UrlManagerCreateUrlTest by
      `testWithNullParams()`
    - `testCreateUrlWithEmptyPattern()`
    - `testCreateAbsoluteUrl()` covered in UrlManagerCreateUrlTest by new
      tests via `variationsProvider()`.
    - `testCreateAbsoluteUrlWithSuffix()` covered in UrlManagerCreateUrlTest
      by `testAbsolutePatterns`.

    - `testParseRequest()` covered by UrlManagerParseUrlTest, UrlNormalizer
      related parts moved to UrlNormalizerTest.
    - `testParseRESTRequest()` moved to UrlManagerParseUrlTest
    - `testHash()` covered in different tests in UrlManagerCreateUrlTest.
    - `testMultipleHostsRules($host)` kept as is.

    Before:

        $ vendor/bin/phpunit tests/framework/web/UrlManagerTest.php
        ...
        OK (12 tests, 89 assertions)

    After:

        $ vendor/bin/phpunit tests/framework/web/UrlManager*.php
        ...
        OK (72 tests, 648 assertions)