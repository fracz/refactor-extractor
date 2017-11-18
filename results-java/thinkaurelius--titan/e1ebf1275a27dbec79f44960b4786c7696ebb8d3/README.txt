commit e1ebf1275a27dbec79f44960b4786c7696ebb8d3
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu May 9 16:40:59 2013 -0400

    Untested refactoring of LocalLockMediatorTest

    Replaced ByteBuffer with StaticBuffer.  The changes required in this
    short test file touch only two lines and seem completely trivial.