commit c8a9f066f79fd106d315064f238a616b37edbb93
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Mon Apr 26 15:25:56 2010 +0000

    unit tests: MDL-22175 Still more improvements unit test failure display.

    Thanks to Eloy for suggesting these improvements:
    1. We display debuginfo if it is there.
    2. If the exception refers to a missing lang string, we output as much
    information as we have.