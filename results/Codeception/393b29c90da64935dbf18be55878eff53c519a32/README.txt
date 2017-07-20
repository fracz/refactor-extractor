commit 393b29c90da64935dbf18be55878eff53c519a32
Author: Luís Cobucci <lcobucci@gmail.com>
Date:   Mon Feb 6 23:19:51 2017 +0100

    Fix small refactoring issue

    On ed549a9d2c5b6d4ccf940a6dcfa671cd042e8f99 an `else` statement was
    removed (awesome) but the author forgot to interrupt the execution
    flow and that is causing our test suite to fail :sob: but this fixes
    it.