commit 9f82ddd66ff413651896a43bb9e343fec1bb1dbd
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Wed Aug 8 13:53:28 2012 +0100

    MDL-34657 datalib: function to generate user sort sql.

    The purpose of this method is to ensure that when we display lists of
    users in different places, the default sort order is consistent.

    Having this code centralised in one place them makes it possible to
    improve things accross the board, and there is an example of that here.
    There is logic so that if we are searching for particular string, then
    users with an exact match are sorted first.