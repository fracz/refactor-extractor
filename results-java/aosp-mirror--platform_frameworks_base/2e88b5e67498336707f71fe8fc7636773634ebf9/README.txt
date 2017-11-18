commit 2e88b5e67498336707f71fe8fc7636773634ebf9
Author: Erik Kline <ek@google.com>
Date:   Wed Jan 18 11:57:45 2017 +0900

    BroadcastReceiver refactoring

    All registerReceiver() calls now specify to be run on the tethering
    master state machine's handler. Eventually, some of the locking might
    be removed (requires careful review, later).

    Also: slightly refactor StateReceiver for readability

    Test: as follows
        - built (bullhead)
        - flashed
        - booted
        - runtest frameworks-net passes
        - vanilla tethering from WiFi to mobile works
    Bug: 32163131
    Change-Id: I39844f6e1608179ebeb41668a6db8a4e44d30ecf