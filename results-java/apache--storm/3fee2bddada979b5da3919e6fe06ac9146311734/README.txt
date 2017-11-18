commit 3fee2bddada979b5da3919e6fe06ac9146311734
Author: Jungtaek Lim <kabhwan@gmail.com>
Date:   Fri Aug 12 22:58:08 2016 +0900

    STORM-2016 Topology submission improvement: support adding local jars and maven artifacts on submission

    *NOTE* This contains a bugfix at creating symbolic link and forward reference in storm.thrift (from STORM-1994)

    * bin/storm now supports "--jars" and "--artifacts" options
    ** it's only effective with "storm jar" and "storm sql"
    * introduce new module: storm-submit-tools to help resolving dependencies with handling transitive dependencies
    * StormSubmitter will upload dependencies to BlobStore when submitting topology
    ** StormSubmitter will remove jars blobs when topology submission fails
    *** remove jar blobs when only one of AlreadyAliveException, InvalidTopologyException, AuthorizationException occurs
    * Supervisor will download dependencies from BlobStore when such topology is assigned
    * Supervisor will launch workers with adding downloaded dependencies to worker classpath
    * Nimbus will remove jars from BlobStore when topology is killed
    ** don't remove artifacts from blobstore since it's shared across topologies
    * add options to document (Command-line-client.md) and also storm.py
    ** it will be printed out when 'help jar' and 'help sql' is called