commit ee802ad63c0f21d697a5095dd05dc6f94626ee4d
Author: Ryan Ernst <ryan@iernst.net>
Date:   Tue Mar 21 23:26:07 2017 -0700

    Test: Add dump of integ test cluster logs on failure (#23688)

    This commit adds a build listener to the integ test runner which will
    print out an excerpt of the logs from the integ test cluster if the test
    run fails.  There are future improvements that can be made (for example,
    to dump excerpts from dependent clusters like in tribe node tests), but
    this is a start, and would help with simple rest tests failures that we
    currently don't know what actually happened on the node.