commit ccd5c0a30903c2a74fbf9dc718fe6bd886a00f21
Author: Ulf Adams <ulfjack@google.com>
Date:   Fri Jan 13 15:46:51 2017 +0000

    Merge the action input prefetcher into the ExecutorBuilder.

    This should be a no-op change, primarily intended to improve the BlazeModule
    API. The code simplification in the distributor code path is incidental.

    --
    PiperOrigin-RevId: 144441458
    MOS_MIGRATED_REVID=144441458