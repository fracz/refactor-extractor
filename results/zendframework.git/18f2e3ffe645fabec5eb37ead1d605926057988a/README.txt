commit 18f2e3ffe645fabec5eb37ead1d605926057988a
Author: sasa <saschaprolic@googlemail.com>
Date:   Sun Oct 10 18:50:21 2010 +0200

    refactoring of Zend\Translator for exceptions milestone finished
    including unit tests, but there are 4 failures !

    TranslatorTest.php

    There were 3 failures:

    1) ZendTest\Translator\TranslatorTest::testReroutingForTranslations
    Failed asserting that <boolean:false> is true.

    tests/Zend/Translator/TranslatorTest.php:751

    2) ZendTest\Translator\TranslatorTest::testCircleReroutingForTranslations
    Failed asserting that <boolean:true> is false.

    tests/Zend/Translator/TranslatorTest.php:782

    3) ZendTest\Translator\TranslatorTest::testDoubleReroutingForTranslations
    Failed asserting that <boolean:true> is false.

    tests/Zend/Translator/TranslatorTest.php:816

    FAILURES!
    Tests: 53, Assertions: 142, Failures: 3.

    Adapter/ArrayTest.php

    There was 1 error:

    1) ZendTest\Translator\Adapter\ArrayTest::testToString
    The language 'de_DE' has to be added before it can be used.

    library/Zend/Translator/Adapter.php:431
    library/Zend/Translator/Adapter.php:184
    tests/Zend/Translator/Adapter/ArrayTest.php:99

    FAILURES!
    Tests: 18, Assertions: 87, Errors: 1.