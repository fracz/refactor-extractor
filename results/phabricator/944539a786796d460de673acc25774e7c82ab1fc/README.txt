commit 944539a786796d460de673acc25774e7c82ab1fc
Author: epriestley <git@epriestley.com>
Date:   Tue Feb 23 16:23:40 2016 -0800

    Simplify locking of Almanac cluster services

    Summary:
    Fixes T6741. Ref T10246. Broadly, we want to protect Almanac cluster services:

      - Today, against users in the Phacility cluster accidentally breaking their own instances.
      - In the future, against attackers compromising administrative accounts and adding a new "cluster database" which points at hardware they control.

    The way this works right now is really complicated: there's a global "can create cluster services" setting, and then separate per-service and per-device locks.

    Instead, change "Can Create Cluster Services" into "Can Manage Cluster Services". Require this permission (in addition to normal permissions) to edit or create any cluster service.

    This permission can be locked to "No One" via config (as we do in the Phacility cluster) so we only need this one simple setting.

    There's also zero reason to individually lock //some// of the cluster services.

    Also improve extended policy errors.

    The UI here is still a little heavy-handed, but should be good enough for the moment.

    Test Plan:
      - Ran migrations.
      - Verified that cluster services and bindings reported that they belonged to the cluster.
      - Edited a cluster binding.
      - Verified that the bound device was marked as a cluster device
      - Moved a cluster binding, verified the old device was unmarked as a cluster device.
      - Tried to edit a cluster device as an unprivileged user, got a sensible error.

    {F1126552}

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T6741, T10246

    Differential Revision: https://secure.phabricator.com/D15339