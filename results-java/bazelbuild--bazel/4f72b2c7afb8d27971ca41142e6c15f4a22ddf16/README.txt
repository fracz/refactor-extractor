commit 4f72b2c7afb8d27971ca41142e6c15f4a22ddf16
Author: Philipp Wollermann <philwo@google.com>
Date:   Mon Jun 6 12:43:01 2016 +0000

    Add --experimental_multi_threaded_digest which lets DigestUtils use multiple threads when calculating the MD5 hash even for large files. Might improve performance when using an SSD.

    Fixes #835 and #1210.

    --
    MOS_MIGRATED_REVID=124128233