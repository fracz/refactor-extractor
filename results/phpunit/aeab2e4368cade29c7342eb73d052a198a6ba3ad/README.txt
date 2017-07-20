commit aeab2e4368cade29c7342eb73d052a198a6ba3ad
Author: Laurent Laffont <laurent.laffont@gmail.com>
Date:   Fri Jan 3 17:22:21 2014 +0100

    Performance improvement when using --filter option with large test code base: PHPUnit_Framework_TestSuite::isTestMethod is the bottleneck.
      * Now 2 times less calls from PHPUnit_Framework_TestSuite::addTestMethod
      * Only 1 call to ReflectionFunctionAbstract::getDocComment

    Conflicts:
            PHPUnit/Framework/TestSuite.php