commit 5a5a2749cdc5abe35be87d94da11802c57ffdde1
Author: Himanshu <g.himanshu@gmail.com>
Date:   Fri Apr 28 08:41:38 2017 -0500

    improvements to coordinator lookups management (#3855)

    * coordinator lookups mgmt improvements

    * revert replaces removal, deprecate it instead

    * convert and use older specs stored in db

    * more tests and updates

    * review comments

    * add behavior for 0.10.0 to 0.9.2 downgrade

    * incorporating more review comments

    * remove explicit lock and use LifecycleLock in LookupReferencesManager. use LifecycleLock in LookupCoordinatorManager as well

    * wip on LookupCoordinatorManager

    * lifecycle lock

    * refactor thread creation into utility method

    * more review comments addressed

    * support smooth roll back of lookup snapshots from 0.10.0 to 0.9.2

    * correctly use LifecycleLock in LookupCoordinatorManager and remove synchronization from start/stop

    * run lookup mgmt on leader coordinator only

    * wip: changes to do multiple start() and stop() on LookupCoordinatorManager

    * lifecycleLock fix usage in LookupReferencesManagerTest

    * add LifecycleLock back

    * fix license hdr

    * some fixes

    * make LookupReferencesManager.getAllLookupsState() consistent while still being lockless

    * address review comments

    * addressing leventov's comments

    * address charle's comments

    * add IOE.java

    * for safety in LookupReferencesManager mainThread check for lifecycle started state on each loop in addition to interrupt

    * move thread creation utility method to Execs

    * fix names

    * add tests for LookupCoordinatorManager.lookupManagementLoop()

    * add further tests for figuring out toBeLoaded and toBeDropped on LookupCoordinatorManager

    * address leventov comments

    * remove LookupsStateWithMap and parameterize LookupsState

    * address review comments

    * address more review comments

    * misc fixes