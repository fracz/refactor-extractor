commit 20d7df3c3ff0000678a208b25fcf7ddf90c5abe4
Author: Adrian Roos <roosa@google.com>
Date:   Tue Jan 12 18:59:43 2016 +0100

    Crash dialog improvements, move crash code to AppErrors

    Factors out the crash and ANR handling code into separate
    class and allows clearing cache and restarting app from
    crash dialog.

    Bug: 22692162
    Change-Id: I2a08a4255ea02ab3c7441d351bf278128fcf5a5d