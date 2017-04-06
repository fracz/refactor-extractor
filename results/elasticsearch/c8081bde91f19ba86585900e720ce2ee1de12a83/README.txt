commit c8081bde91f19ba86585900e720ce2ee1de12a83
Author: Lee Hinman <lee@writequit.org>
Date:   Thu Mar 9 21:07:29 2017 -0700

    Further refactor and extend testing for `TransportShardBulkAction`

    This moves `updateReplicaRequest` to `createPrimaryResponse` and separates the
    translog updating to be a separate function so that the function purpose is more
    easily understood (and testable).

    It also separates the logic for `MappingUpdatePerformer` into two functions,
    `updateMappingsIfNeeded` and `verifyMappings` so they don't do too much in a
    single function. This allows finer-grained error testing for when a mapping
    fails to parse or be applied.

    Finally, it separates parsing and version validation for
    `executeIndexRequestOnReplica` into a separate
    method (`prepareIndexOperationOnReplica`) and adds a test for it.

    Relates to #23359