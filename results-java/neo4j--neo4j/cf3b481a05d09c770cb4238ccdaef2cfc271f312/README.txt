commit cf3b481a05d09c770cb4238ccdaef2cfc271f312
Author: tinwelint <mattias@neotechnology.com>
Date:   Mon Jan 16 14:05:09 2017 +0100

    GBPTreeIT have more consistent test times

    Previously there were some that took sub-second, others tens of seconds.
    Execution time of most tests were also dependent on how many threads the
    machine running it had, this is also improved on.