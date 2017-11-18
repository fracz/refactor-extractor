commit e63cec592f51e8e38969c8e3b1b162f63fa3872f
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Fri Nov 1 22:11:45 2013 -0700

    Made it mandatory for all storage backends to provide key consistency and removed TransactionalIDAuthority. Now, all stores use the KeyConsistentIDAuthority which allows operating transactional stores with disabled transactions without data corruption.
    This refactoring effectively de-couples transactionality from the id allocation mechanism which makes it safer and the code easier.

    Note, that this introduces an incompatibility with prior releases for the following backends:
    BerkeleyDB, Persistit, and Hazelcast
    because their id allocation mechanism has changed.

    Hence, this branch should only be merged with the next MAJOR release branch.