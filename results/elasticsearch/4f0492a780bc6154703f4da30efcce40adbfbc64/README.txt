commit 4f0492a780bc6154703f4da30efcce40adbfbc64
Author: Simon Willnauer <simonw@apache.org>
Date:   Mon Apr 27 18:58:01 2015 +0200

    [TEST] Run tests with 1 or 2 nodes by default

    This commit adds support for running with only one node and sets the
    maximum number of nodes to 3 by default. if run with test.nighly=true
    at most 6 nodes are used. This gave a 20% speed improvement compared to
    the previoulys minimum number of nodes of 3.