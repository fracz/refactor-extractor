commit 751bd52510b46ec6f7c048629e3a7149336d8e82
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Fri Jan 13 19:58:57 2017 +0100

    refactor local database / data source interaction

    Make it so that local database just registers the dependencies
    from the data source in the commit process factory callback,
    instead of using them immediately for consensus log index
    recovery and the subsequent "install" of the commit process
    and the last applied index. This is now performed later.

    This allows us to start local database earlier for store recovery
    purposes. Previously, since the transaction log itself had not
    gone through the store recovery process, it could lead to issues.
    An example being a partially written transaction which normally
    should get truncated during recovery, but instead would be
    detected as a format error and thus prevent startup.