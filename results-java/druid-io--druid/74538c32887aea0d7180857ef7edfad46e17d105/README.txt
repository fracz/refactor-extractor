commit 74538c32887aea0d7180857ef7edfad46e17d105
Author: Himanshu <g.himanshu@gmail.com>
Date:   Fri Aug 25 11:28:15 2017 -0500

    update internal-discovery Listener for node list and use same at router and coordinator (#4697)

    * update LookupCoordinatorManager to use internal discovery to discover lookup nodes

    * router:use internal-discovery to discover brokers

    * minor [Curator]DruidDiscoveryProvider refactoring

    * add initialized() method to DruidNodeDiscovery.Listener

    * update HttpServerInventoryView to use initialized() and call segment callback initialization asynchronously

    * Revert "update HttpServerInventoryView to use initialized() and call segment callback initialization asynchronously"

    This reverts commit f796e441221fe8b0e9df87fdec6c9f47bcedd890.

    * Revert "add initialized() method to DruidNodeDiscovery.Listener"

    This reverts commit f0661541d073683f28fce2dd4f30ec37db90deb0.

    * minor refactoring removing synchronized from DruidNodeDiscoveryProvider

    * updated DruidNodeDiscovery.Listener contract to take List of nodes and first call marks initialization

    * update HttpServerInventoryView to handle new contract and handle initialization

    * router update to handle updated listener contract

    * document DruidNodeDiscovery.Listener contract

    * fix forbidden-api error

    * change log level to info for unknown path children cache events in CuratorDruidNodeDiscoveryProvider

    * announce broker only after segment inventory is initialized