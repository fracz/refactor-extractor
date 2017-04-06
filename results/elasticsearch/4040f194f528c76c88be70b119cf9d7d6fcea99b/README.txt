commit 4040f194f528c76c88be70b119cf9d7d6fcea99b
Author: Robert Muir <rmuir@apache.org>
Date:   Wed Jul 22 10:44:52 2015 -0400

    Refactor pluginservice

    Closes #12367

    Squashed commit of the following:

    commit 9453c411798121aa5439c52e95301f60a022ba5f
    Merge: 3511a9c 828d8c7
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Jul 22 08:22:41 2015 -0400

        Merge branch 'master' into refactor_pluginservice

    commit 3511a9c616503c447de9f0df9b4e9db3e22abd58
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Tue Jul 21 21:50:15 2015 -0700

        Remove duplicated constant

    commit 4a9b5b4621b0ef2e74c1e017d9c8cf624dd27713
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Tue Jul 21 21:01:57 2015 -0700

        Add check that plugin must specify at least site or jvm

    commit 19aef2f0596153a549ef4b7f4483694de41e101b
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Tue Jul 21 20:52:58 2015 -0700

        Change plugin "plugin" property to "classname"

    commit 07ae396f30ed592b7499a086adca72d3f327fe4c
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Jul 21 23:36:05 2015 -0400

        remove test with no methods

    commit 550e73bf3d0f94562f4dde95239409dc5a24ce25
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Jul 21 23:31:58 2015 -0400

        fix loading to use classname

    commit 04463aed12046da0da5cac2a24c3ace51a79f799
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Jul 21 23:24:19 2015 -0400

        rename to classname

    commit 9f3afadd1caf89448c2eb913757036da48758b2d
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Tue Jul 21 20:18:46 2015 -0700

        moved PluginInfo and refactored parsing from properties file

    commit df63ccc1b8b7cc64d3e59d23f6c8e827825eba87
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Jul 21 23:08:26 2015 -0400

        fix test

    commit c7febd844be358707823186a8c7a2d21e37540c9
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Jul 21 23:03:44 2015 -0400

        remove test

    commit 017b3410cf9d2b7fca1b8653e6f1ebe2f2519257
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Jul 21 22:58:31 2015 -0400

        fix test

    commit c9922938df48041ad43bbb3ed6746f71bc846629
    Merge: ad59af4 01ea89a
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Jul 21 22:37:28 2015 -0400

        Merge branch 'master' into refactor_pluginservice

    commit ad59af465e1f1ac58897e63e0c25fcce641148a7
    Author: Areek Zillur <areek.zillur@elasticsearch.com>
    Date:   Tue Jul 21 19:30:26 2015 -0400

        [TEST] Verify expected number of nodes in cluster before issuing shardStores request

    commit f0f5a1e087255215b93656550fbc6bd89b8b3205
    Author: Lee Hinman <lee@writequit.org>
    Date:   Tue Jul 21 11:27:28 2015 -0600

        Ignore EngineClosedException during translog fysnc

        When performing an operation on a primary, the state is captured and the
        operation is performed on the primary shard. The original request is
        then modified to increment the version of the operation as preparation
        for it to be sent to the replicas.

        If the request first fails on the primary during the translog sync
        (because the Engine is already closed due to shadow primaries closing
        the engine on relocation), then the operation is retried on the new primary
        after being modified for the replica shards. It will then fail due to the
        version being incorrect (the document does not yet exist but the request
        expects a version of "1").

        Order of operations:

        - Request is executed against primary
        - Request is modified (version incremented) so it can be sent to replicas
        - Engine's translog is fsync'd if necessary (failing, and throwing an exception)
        - Modified request is retried against new primary

        This change ignores the exception where the engine is already closed
        when syncing the translog (similar to how we ignore exceptions when
        refreshing the shard if the ?refresh=true flag is used).

    commit 4ac68bb1658688550ced0c4f479dee6d8b617777
    Author: Shay Banon <kimchy@gmail.com>
    Date:   Tue Jul 21 22:37:29 2015 +0200

        Replica allocator unit tests
        First batch of unit tests to verify the behavior of replica allocator

    commit 94609fc5943c8d85adc751b553847ab4cebe58a3
    Author: Jason Tedor <jason@tedor.me>
    Date:   Tue Jul 21 14:04:46 2015 -0400

        Correctly list blobs in Azure storage to prevent snapshot corruption and do not unnecessarily duplicate Lucene segments in Azure Storage

        This commit addresses an issue that was leading to snapshot corruption for snapshots stored as blobs in Azure Storage.

        The underlying issue is that in cases when multiple snapshots of an index were taken and persisted into Azure Storage, snapshots subsequent
        to the first would repeatedly overwrite the snapshot files. This issue does render useless all snapshots except the final snapshot.

        The root cause of this is due to String concatenation involving null. In particular, to list all of the blobs in a snapshot directory in
        Azure the code would use the method listBlobsByPrefix where the prefix is null. In the listBlobsByPrefix method, the path keyPath + prefix
        is constructed. However, per 5.1.11, 5.4 and 15.18.1 of the Java Language Specification, the reference null is first converted to the string
        "null" before performing the concatenation. This leads to no blobs being returned and therefore the snapshot mechanism would operate as if
        it were writing the first snapshot of the index. The fix is simply to check if prefix is null and handle the concatenation accordingly.

        Upon fixing this issue so that subsequent snapshots would no longer overwrite earlier snapshots, it was discovered that the snapshot metadata
        returned by the listBlobsByPrefix method was not sufficient for the snapshot layer to detect whether or not the Lucene segments had already
        been copied to the Azure storage layer in an earlier snapshot. This led the snapshot layer to unnecessarily duplicate these Lucene segments
        in Azure Storage.

        The root cause of this is due to known behavior in the CloudBlobContainer.getBlockBlobReference method in the Azure API. Namely, this method
        does not fetch blob attributes from Azure. As such, the lengths of all the blobs appeared to the snapshot layer to be of length zero and
        therefore they would compare as not equal to any new blobs that the snapshot layer is going to persist. To remediate this, the method
        CloudBlockBlob.downloadAttributes must be invoked. This will fetch the attributes from Azure Storage so that a proper comparison of the
        blobs can be performed.

        Closes elastic/elasticsearch-cloud-azure#51, closes elastic/elasticsearch-cloud-azure#99

    commit cf1d481ce5dda0a45805e42f3b2e0e1e5d028b9e
    Author: Lee Hinman <lee@writequit.org>
    Date:   Mon Jul 20 08:41:55 2015 -0600

        Unit tests for `nodesAndVersions` on shared filesystems

        With the `recover_on_any_node` setting, these unit tests check that the
        correct node list and versions are returned.

    commit 3c27cc32395c3624f7c794904d9ea4faf2eccbfb
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Jul 21 14:15:59 2015 -0400

        don't fail junit4 integration tests if there are no tests.

        instead fail the failsafe plugin, which means the external cluster will still get shut down

    commit 95d2756c5a8c21a157fa844273fc83dfa3c00aea
    Author: Alexander Reelsen <alexander@reelsen.net>
    Date:   Tue Jul 21 17:16:53 2015 +0200

        Testing: Fix help displaying tests under windows

        The help files are using a unix based file separator, where as
        the test relies on the help being based on the file system separator.

        This commit fixes the test to remove all `\r` characters before
        comparing strings.

        The test has also been moved into its own CliToolTestCase, as it does
        not need to be an integration test.

    commit 944f06ea36bd836f007f8eaade8f571d6140aad9
    Author: Clinton Gormley <clint@traveljury.com>
    Date:   Tue Jul 21 18:04:52 2015 +0200

        Refactored check_license_and_sha.pl to accept a license dir and package path

        In preparation for the move to building the core zip, tar.gz, rpm, and deb as separate modules, refactored check_license_and_sha.pl to:

        * accept a license dir and path to the package to check on the command line
        * to be able to extract zip, tar.gz, deb, and rpm
        * all packages except rpm will work on Windows

    commit 2585431e8dfa5c82a2cc5b304cd03eee9bed7a4c
    Author: Chris Earle <pickypg@users.noreply.github.com>
    Date:   Tue Jul 21 08:35:28 2015 -0700

        Updating breaking changes

        - field names cannot be mapped with `.` in them
        - fixed asciidoc issue where the list was not recognized as a list

    commit de299b9d3f4615b12e2226a1e2eff5a38ecaf15f
    Author: Shay Banon <kimchy@gmail.com>
    Date:   Tue Jul 21 13:27:52 2015 +0200

        Replace primaryPostAllocated flag and use UnassignedInfo
        There is no need to maintain additional state as to if a primary was allocated post api creation on the index routing table, we hold all this information already in the UnassignedInfo class.
        closes #12374

    commit 43080bff40f60bedce5bdbc92df302f73aeb9cae
    Author: Alexander Reelsen <alexander@reelsen.net>
    Date:   Tue Jul 21 15:45:05 2015 +0200

        PluginManager: Fix bin/plugin calls in scripts/bats test

        The release and smoke test python scripts used to install
        plugins in the old fashion.

        Also the BATS testing suite installed/removed plugins in that
        way. Here the marvel tests have been removed, as marvel currently
        does not work with the master branch.

        In addition documentation has been updated as well, where it was
        still missing.

    commit b81ccba48993bc13c7678e6d979fd96998499233
    Author: Boaz Leskes <b.leskes@gmail.com>
    Date:   Tue Jul 21 11:37:50 2015 +0200

        Discovery: make sure NodeJoinController.ElectionCallback is always called from the update cluster state thread

        This is important for correct handling of the joining thread. This causes assertions to trip in our test runs. See http://build-us-00.elastic.co/job/es_g1gc_master_metal/11653/ as an example

        Closes #12372

    commit 331853790bf29e34fb248ebc4c1ba585b44f5cab
    Author: Boaz Leskes <b.leskes@gmail.com>
    Date:   Tue Jul 21 15:54:36 2015 +0200

        Remove left over no commit from TransportReplicationAction

        It asks to double check thread pool rejection. I did and don't see problems with it.

    commit e5724931bbc1603e37faa977af4235507f4811f5
    Author: Alexander Reelsen <alexander@reelsen.net>
    Date:   Tue Jul 21 15:31:57 2015 +0200

        CliTool: Various PluginManager fixes

        The new plugin manager parser was not called correctly in the scripts.
        In addition the plugin manager now creates a plugins/ directory in case
        it does not exist.

        Also the integration tests called the plugin manager in the deprecated way.

    commit 7a815a370f83ff12ffb12717ac2fe62571311279
    Author: Alexander Reelsen <alexander@reelsen.net>
    Date:   Tue Jul 21 13:54:18 2015 +0200

        CLITool: Port PluginManager to use CLITool

        In order to unify the handling and reuse the CLITool infrastructure
        the plugin manager should make use of this as well.

        This obsolets the -i and --install options but requires the user
        to use `install` as the first argument of the CLI.

        This is basically just a port of the existing functionality, which
        is also the reason why this is not a refactoring of the plugin manager,
        which will come in a separate commit.

    commit 7f171eba7b71ac5682a355684b6da703ffbfccc7
    Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
    Date:   Tue Jul 21 10:44:21 2015 +0200

        Remove custom execute local logic in TransportSingleShardAction and TransportInstanceSingleOperationAction and rely on transport service to execute locally. (forking thread etc.)

        Change TransportInstanceSingleOperationAction to have shardActionHandler to, so we can execute locally without endless spinning.

    commit 0f38e3eca6b570f74b552e70b4673f47934442e1
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Tue Jul 21 17:36:12 2015 -0700

        More readMetadata tests and pickiness

    commit 880b47281bd69bd37807e8252934321b089c9f8e
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Tue Jul 21 14:42:09 2015 -0700

        Started unit tests for plugin service

    commit cd7c8ddd7b8c4f3457824b493bffb19c156c7899
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Jul 21 07:21:07 2015 -0400

        fix tests

    commit 673454f0b14f072f66ed70e32110fae4f7aad642
    Author: Robert Muir <rmuir@apache.org>
    Date:   Tue Jul 21 06:58:25 2015 -0400

        refactor pluginservice