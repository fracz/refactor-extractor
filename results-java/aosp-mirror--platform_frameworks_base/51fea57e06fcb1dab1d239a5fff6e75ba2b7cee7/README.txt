commit 51fea57e06fcb1dab1d239a5fff6e75ba2b7cee7
Author: Christopher Tate <ctate@google.com>
Date:   Mon Jun 23 17:01:06 2014 -0700

    Refactor restore to deal with heterogeneous datasets

    Transport-based restore now handles both key/value and full-data
    (stream) data delivery.

    Also: PMBA now holds metadata for *all* apps, not just those with
    backup agents.  Since we need to consult this for every restore-
    at-install operation we cache this locally now, tagged per transport
    and per remote dataset, to avoid having to re-download it as part
    of every future restore operation.

    Also fixed a bug in LocalTransport that was preventing restore of
    key/value datasets, i.e. all of them that were nominally available
    prior to this change.

    NOTE: at present there is no automatic full-data backup; if for
    testing purposes you need to create some to then use for restore,
    you still need to use 'bmgr fullbackup ...' to push them.

    NOTE: at present the unified transport restore uses a refactored
    "engine" implementation to handle stream data that encapsulates
    the existing "adb restore" implementation.  However, the adb
    restore code path has not yet been refactored to wrap the newly-
    extracted engine version; it still contains its own copy of all
    the relevant logic.  This will change in a future CL, at which
    point offline/USB archive restore will simply wrap the same
    shared stream-restore engine implementation.

    Bug 15330073
    Bug 15989617

    Change-Id: Ieedb18fd7836ad31ba24656ec9feaaf69e164af8