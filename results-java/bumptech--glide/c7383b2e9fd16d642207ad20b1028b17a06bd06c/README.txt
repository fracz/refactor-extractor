commit c7383b2e9fd16d642207ad20b1028b17a06bd06c
Author: judds <judds@google.com>
Date:   Tue Feb 7 16:08:01 2017 -0800

    Fix integer overflow bug in Downsampler when asked to upscale images.

    This should also be a slight accuracy improvement since we're now using
    a somewhat more efficient density multiplier in some cases, especially
    when downsampling.

    Fixes #2459.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=171601510