commit 93c4afae07e15b89a1aaacdb5242cefa1c0c2fb2
Author: Erik Kline <ek@google.com>
Date:   Sun Jun 4 11:36:01 2017 +0900

    Minor upstream selection refactoring

    Specifically:
        - relocated "tryCell" handling
        - minor logging change
        - remove unneeded checkExpectedThread()

    Test: as follows
        - built
        - flashed
        - booted
        - runtest frameworks-net passes
    Bug: 32163131
    Change-Id: I2f5428206503fd222b959e695c26326df53038f1