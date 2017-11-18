commit a66b4a9badf58487dd46ecefb4c6c2d001cda2d4
Author: aquilescanta <aquilescanta@google.com>
Date:   Tue Dec 6 08:33:30 2016 -0800

    Parse ID3 sample timestamp for HLS audio chunks

    Pending improvement:

    * Peek just the required priv frame. Avoid decoding all id3 information.
    * Sniff the used container format instead of using the extension.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=141181781