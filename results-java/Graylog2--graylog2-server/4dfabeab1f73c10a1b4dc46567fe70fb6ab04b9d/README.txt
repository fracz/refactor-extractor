commit 4dfabeab1f73c10a1b4dc46567fe70fb6ab04b9d
Author: Bernd Ahlers <bernd@torch.sh>
Date:   Sat Dec 13 17:36:09 2014 +0100

    New stream router implementation.

    This greatly improves the stream matching throughput.

    Since we do not iterate over streams and executing their rules in stream
    order, we lose the executionTime metric for streams. We are getting the
    executionTime per stream rule instead.

    The stream router engine is rebuilt periodically but only replaces the
    existing one if streams or streamrules changed.

    This also adds some performance optimizations for the Message and
    PersistedImpl classes.

    The new code only executes REGEX stream rules with a timeout because
    the other code should not hang.

    Move stream fault handling and stream metrics into a separate class.