commit d9e1f90b17caa668584e218572fb04ef56825f3d
Author: Walt Sorensen <photodude@users.noreply.github.com>
Date:   Sun Dec 11 04:40:47 2016 -0700

    [Unit Test improvements] Use getMockBuilder rather than getMock directly  (#12990)

    * Use getMockBuilder rather than getMock directly

    Avoid situations that would cause `PHP Fatal error:  Call to protected method PHPUnit_Framework_TestCase::getMock()` on newer versions of phpunit

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use the correct $this object

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Set up a more mock JTableExtension with getMockBuilder

    * Use getMockBuilder rather than getMock directly

    * Use the correct $this object

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * remove the method after replacing

    * Use getMockBuilder rather than getMock directly

    * More getMockBuilder in place of getMock

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Fix testParse mock Constructor Args

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * disableOriginalConstructor

    * disableOriginalConstructor()

    * disableOriginalConstructor()

    * disableOriginalConstructor()

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * another spot for getMockBuilder

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * remove duplicate $this->transport

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Use getMockBuilder rather than getMock directly

    * Add 'setUserState' to MockApplicationCms

    Fixes 2 warnings on phpUnit 5.6:
    1) JModelListTest::testGetuserstateUsesDefault
    Trying to configure method "setUserState" which cannot be configured because it does not exist, has not been specified, is final, or is static
    2) JModelListTest::testGetuserstateUsesRequestData
    Trying to configure method "setUserState" which cannot be configured because it does not exist, has not been specified, is final, or is static

    * mockDatabase fails for mockTableExtension on hhvm

    Fixes 10 hhvm failures that have one of the following messages
    ```php
    UnexpectedValueException: No columns found for #__extensions table

    Expectation failed for method name is equal to <string:parseSchemaUpdates> when invoked 1 time(s).
    Method was expected to be called 1 times, actually called 0 times.
    ```

    * cs-fix extra semicolon

    * remove trailing coma

    * remove trailing comma