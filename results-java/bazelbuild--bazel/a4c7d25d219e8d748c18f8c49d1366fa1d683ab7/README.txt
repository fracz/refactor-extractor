commit a4c7d25d219e8d748c18f8c49d1366fa1d683ab7
Author: Klaus Aehlig <aehlig@google.com>
Date:   Fri May 20 10:44:21 2016 +0000

    experimental UI: improve message shortening

    When a progress message has to be shortened, as it does not fit in a
    line in the progress bar, add a new first attempt: if the message the
    path implicit to the label, only shorten that path within the message
    (if that gets short enough, leaving a reasonable part of the path);
    usually, the additional information is more useful than having a longer
    part of the path present.

    While there, also fix incorrect length computation in a different case
    of message shortening.

    --
    Change-Id: Ied80e03cace1b249fc0f4e11bce41f2b4207b6ad
    Reviewed-on: https://bazel-review.googlesource.com/#/c/3670
    MOS_MIGRATED_REVID=122818198