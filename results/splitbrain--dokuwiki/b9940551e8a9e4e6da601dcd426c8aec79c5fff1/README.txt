commit b9940551e8a9e4e6da601dcd426c8aec79c5fff1
Author: Andreas Gohr <gohr@cosmocode.de>
Date:   Thu Dec 1 11:36:17 2016 +0100

    refactored the remote API tests

    Now each command is checked within it's own test on a completely clean
    install. Hopefuly this also takes care of the flaky test behavior seen
    at Travis occasionally.