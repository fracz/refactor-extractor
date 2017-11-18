commit 299fd834ac9a19e89da080f55c4d49cf7c27be01
Author: Tomasz Bak <tbak@netflix.com>
Date:   Sun Oct 4 21:54:33 2015 -0700

    Add missing EurekaHttpClient methods + tests refactoring.

    Vip query methods, and related support in DiscoveryClient were not
    implemented.
    The single, very long running test was refactored to shorten its
    execution time.