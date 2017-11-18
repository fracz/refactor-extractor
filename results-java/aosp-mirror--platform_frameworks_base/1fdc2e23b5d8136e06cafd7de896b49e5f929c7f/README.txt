commit 1fdc2e23b5d8136e06cafd7de896b49e5f929c7f
Author: Erik Kline <ek@google.com>
Date:   Mon May 8 17:56:35 2017 +0900

    Refactor and improve logging.

    Test: as follows
        - build
        - flashed
        - booted
        - "runtest frameworks-net" passed
        - "dumpsys connectivity" shows new log output
    Bug: 32163131
    Bug: 36504926

    Change-Id: I14d6da18660223f7cace156cb6594ee18928a7b0