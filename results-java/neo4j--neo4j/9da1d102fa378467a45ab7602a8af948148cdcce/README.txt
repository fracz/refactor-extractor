commit 9da1d102fa378467a45ab7602a8af948148cdcce
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Mon Jan 21 16:07:05 2013 +0200

    Introduces a more descriptive package layout for HA

    Packages in HA are now separated based on component functionality. Initial separation intoduces packages for
     transaction, management, lock, com and (entity) id.

    Also removes the obsolete MasterIdGeneratorFactory, ExecutorLifecycleAdapter and refactors away
     TransactionSupport and the single implementation of that.