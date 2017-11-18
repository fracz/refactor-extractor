commit 8ea030bcc901fedf1c4bdb4d7171da948ee6950f
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu May 9 15:37:59 2013 -0400

    Untested refactoring of titan-test KeyValueStore*

    This is a first attempt at getting KeyValueStoreTest and -Util to use
    ReadBuffer and StaticBuffer instead of ByteBuffer (where appropriate)