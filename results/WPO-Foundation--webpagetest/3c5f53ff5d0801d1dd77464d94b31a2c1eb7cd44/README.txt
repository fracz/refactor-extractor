commit 3c5f53ff5d0801d1dd77464d94b31a2c1eb7cd44
Author: Stefan Burnicki <stefan.burnicki@iteratec.de>
Date:   Mon Jun 20 10:50:13 2016 +0200

    refactor: Made 'requires' location independent, fixed tests

    Added the __DIR__ constant to some 'require' directives, so they
    work independent from the working directory.

    I also fixed the graphPageData tests (missing arguments, expected
    results diffferent to current implementation), so all tests are
    green with the current implementation.