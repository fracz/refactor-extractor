commit 8b86697e79532204ee556383c64f86ef8e10dfa2
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu Feb 6 20:50:22 2014 -0500

    Adding timeout parameter to IDAuthority#getIDBlock

    Instead of relying on a hardcoded retry count, getIDBlock now takes a
    timeout long and accompanying TimeUnit.  Implementations return if
    they are unable to allocate a block within the timeout duration.  But
    really only the ConsistentKeyIDManager implementation respects the
    timeout; MockIDManager doesn't bother.

    I think idApplicationRetryCount could be refactored with a more
    descriptive name now, since its only function is to limit the number
    of randomized unique id exhaustions allowed before declaring the whole
    partition exhausted.