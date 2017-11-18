commit 1fb917c6718e1794aba78fe06b65620eb1b5afa0
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu May 9 16:59:10 2013 -0400

    Untested refactoring of SerializerTest

    This is a first attempt at getting SerializerTest to use StaticBuffer,
    ReadBuffer, and WriteBuffer instead of ByteBuffer (where appropriate)