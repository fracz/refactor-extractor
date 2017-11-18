commit bcf114c2bbef4dd4af266a635a74076d568d125c
Author: Maksymilian Osowski <maxosowski@google.com>
Date:   Fri Aug 27 17:11:14 2010 +0100

    Fixed and slightly improved crash detection mechanism.

    Before, if a crash was detected, but the "Force quit" dialog remained on the screen, sending the intent to restart the executor would do nothing. It's fixed now with default
    uncaught exception handler. Also, when we catch the uncaught exception, we can restart the executor straight away, without waiting for the time-out.

    Change-Id: I2f0b4b5f2abd180ff518f1a40ad1294bed2f7f67