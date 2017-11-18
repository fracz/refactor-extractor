commit c62db8545ae2fd4c4b0144501de50b926b949773
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu May 9 16:03:51 2013 -0400

    Untested refactoring of titan-test KCVStore*

    This is a first attempt at getting KeyColumnValueStoreTest and -Util
    to use ReadBuffer and StaticBuffer instead of ByteBuffer (where
    appropriate)