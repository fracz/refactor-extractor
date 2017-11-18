commit f11f2c4af9105190c6ca5e961df54cf226301b0e
Author: Adrian Cole <acole@pivotal.io>
Date:   Mon May 2 18:05:51 2016 +0800

    Refactors Cassandra codebase so as to only depend on Session

    This is a first stab of code cleaning, mostly refactoring state around
    the Cassandra connection used and removing functional complexity.