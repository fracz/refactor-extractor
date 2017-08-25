commit 3de4c7c77818125e660aebf5af3a331a989873c3
Author: Théo FIDRY <theo.fidry@gmail.com>
Date:   Mon Oct 31 23:22:28 2016 +0000

    Fix support for stdClass classes (#595)

    Accessing a value from an stdClass instance with the Symfony property
    accessor will fail as the class has no public properties neither
    getters. Besides fixing that, the code has been refactored a bit to
    introduce a new property accessor properly supporting stdClass instances.