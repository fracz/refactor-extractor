commit 958707cbf25607aff887f3c72ecabc71891c580d
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu May 9 16:35:06 2013 -0400

    Untested refactoring of Lock and MultiWrite tests

    This is a first attempt at getting LockKeyColumnValueStoreTest and
    MultiWriteKeyColumnValueStoreTest to use StaticBuffer instead of
    ByteBuffer (where appropriate)