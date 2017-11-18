commit 47cb916ec6f41b8ecbd377ed875f842c3d349b12
Author: Ulf Adams <ulfjack@google.com>
Date:   Fri Sep 18 08:12:30 2015 +0000

    Move getOutputFilesystem to CommandEnvironment; refactor BlazeRuntime commands.

    This change makes it so commands are no longer both stored in the BlazeRuntime
    and in the BlazeCommandDispatcher. Instead, they are only stored in
    BlazeRuntime and usually passed there during construction. We have some tests
    where this is tricky, so I'm keeping the old code path for now.

    --
    MOS_MIGRATED_REVID=103364581