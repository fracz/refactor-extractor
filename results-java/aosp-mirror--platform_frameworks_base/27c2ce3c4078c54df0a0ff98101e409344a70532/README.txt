commit 27c2ce3c4078c54df0a0ff98101e409344a70532
Author: Diego Perez <diegoperez@google.com>
Date:   Tue Dec 13 18:10:03 2016 +0000

    Refactor tests structure

    Move all the render test utility methods to a separate class and leave
    only tests in RenderTests class (old Main).
    The idea is to improve the organization to allow to create a set of
    performance tests and also to allow the tests to grow more structured.

    Test: Just moved tests around
    Change-Id: I3b773d0745700dd2a52f937b9b668a2d374b8686