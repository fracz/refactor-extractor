commit 5ab77dcd64b39b8224525e4c551e9335bd06a42d
Author: Bao-Long Nguyen-Trong <baolongnt@gmail.com>
Date:   Mon May 11 04:39:03 2009 +0000

    Refactored message loading for view
    --> Perfomance improvement: the code was doing duplicates calls to the server to set SEEN flag and duplicate access to the db.