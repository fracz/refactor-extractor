commit 58f8774fa25ed749490a3cdef63fc6c5e15ce8b6
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Wed Jun 11 15:54:47 2014 +0200

    [Discovery] do not use versions to optimize cluster state copying for a first update from a new master

    We have an optimization which compares routing/meta data version of cluster states and tries to reuse the current object if the versions are equal. This can cause rare failures during recovery from a minimum_master_node breach when using the "new light rejoin" mechanism and simulated network disconnects. This happens where the current master updates it's state, doesn't manage to broadcast it to other nodes due to the disconnect and then steps down. The new master will start with a previous version and continue to update it. When the old master rejoins, the versions of it's state can equal but the content is different.

    Also improved DiscoveryWithNetworkFailuresTests to simulate this failure (and other improvements)

    Closes #6466