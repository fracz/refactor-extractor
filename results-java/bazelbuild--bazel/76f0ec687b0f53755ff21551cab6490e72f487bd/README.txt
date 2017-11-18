commit 76f0ec687b0f53755ff21551cab6490e72f487bd
Author: Ulf Adams <ulfjack@google.com>
Date:   Wed May 25 12:25:53 2016 +0000

    Enable interleaved loading & analysis by default.

    Fix a bunch of tests to assume interleaving instead of disrete phases.

    In our testing, this improves loading+analysis times by ~30%.

    --
    MOS_MIGRATED_REVID=123203752