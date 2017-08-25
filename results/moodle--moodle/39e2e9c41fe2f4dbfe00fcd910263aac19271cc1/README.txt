commit 39e2e9c41fe2f4dbfe00fcd910263aac19271cc1
Author: Petr Skoda <commits@skodak.org>
Date:   Tue Apr 10 13:58:47 2012 +0200

    MDL-32323 prevent modification of our heavily modified runBare() methods

    This should help with compatibility of future PHPUnit and it also allows us to improve test reset without breaking existing moodle test cases.