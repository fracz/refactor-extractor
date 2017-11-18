commit 699deaf6103d182dbd291ca2b420f30b439753d4
Author: Sundeep Ghuman <sghuman@google.com>
Date:   Mon Feb 13 15:32:13 2017 -0800

    Move Badging from ScoredNetwork to NetworkingBadging.

    This is a non-functional refactor. The old enums will be removed once
    ag/35323372 is addressed.

    Bug: 35114358
    Test: Ran existing tests (see files touched).
    Change-Id: I08fd8c7964463b5908ce361e61f8fe811d0ff6f3