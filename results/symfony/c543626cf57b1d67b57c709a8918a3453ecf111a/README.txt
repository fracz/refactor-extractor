commit c543626cf57b1d67b57c709a8918a3453ecf111a
Author: Henrik Bjørnskov <henrik@bearwoods.dk>
Date:   Wed Dec 15 19:42:24 2010 +0100

    [DoctrineMongoDBBundle] Fixed DoctrineMongoDBExtension::loadConnections to follow the new constructor signature introduced with the Doctrine\\MongoDB access layer refactoring so that Doctrine MongoDB logging works againg with the WebProfilerBundle