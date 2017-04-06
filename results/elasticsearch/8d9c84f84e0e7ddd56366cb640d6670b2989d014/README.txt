commit 8d9c84f84e0e7ddd56366cb640d6670b2989d014
Author: Shay Banon <kimchy@gmail.com>
Date:   Fri Jul 5 17:29:35 2013 -0700

    optimize guice injector once created

    in guice, we always use eager loaded singletons for all modules we create, thus, we can actually optimize the memory used by injectors by reduced the construction information they store per binding resulting in extensive reduction in memory usage for many indices/shards case on a node

     also because all are eager singletons (and effectively, read only), we can not go through trying to create just in time bindings in the parent injector before trying to craete it in the current injector, resulting in improvement of object creations time and the time it takes to create an index or a shard on a node