commit 902ab48820e78efc61b77fa1292b0d25c5355160
Author: Trustin Lee <trustin@gmail.com>
Date:   Tue Nov 17 15:11:31 2009 +0000

    * Rewrote FrameDecoder by utilizing the latest improvement in CompositeChannelBuffer
    ** A user doesn't need to make a copy of the cumulative buffer anymore.
    *** Modified all FrameDecoder subtypes to use slice() instead of readBytes()
    * Reduced the maximum length of the random writes in AbstractSocketFixedLengthEchoTest to increase the probability of composite buffer occurances