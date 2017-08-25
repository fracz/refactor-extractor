commit ab1fef6644927b857c64c9c153915d50ef2607f1
Author: Christopher Smith <chris@jalakai.co.uk>
Date:   Tue Nov 5 20:51:07 2013 +0000

    Test to ensure less can parse the @import rewritten by css_loadfile()

    This test is horrible, but I believe necessary to ensure that the
    @import of less files actually works.

    It is horrible as its not a unit test and its not a true functional
    test.  It probably implies the code in css_out() should be refactored
    to make it easier to test intermediate results.