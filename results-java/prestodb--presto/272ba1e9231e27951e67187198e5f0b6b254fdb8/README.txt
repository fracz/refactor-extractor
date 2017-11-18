commit 272ba1e9231e27951e67187198e5f0b6b254fdb8
Author: Nileema Shingte <nileema.shingte@gmail.com>
Date:   Sun Jun 19 12:26:11 2016 -0700

    Minor refactoring of OrganizationSet

    An organization set can only have shards from the same bucket, so
    refactor the class to hold the set of shard uuids and bucket number.