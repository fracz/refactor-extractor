commit 6382015f9df6cc63e1809f2775ad1d5427247af5
Author: Eric Anderson <ejona@google.com>
Date:   Sat Apr 9 00:30:26 2016 -0700

    internal: Split-state AbstractStream; sending and receiving

    This introduces an AbstractStream2 that is intended to replace the
    current AbstractStream. Only server-side is implemented in this commit
    which is why AbstractStream remains. This is mostly a reorganization of
    AbstractStream and children, but minor internal behavioral changes were
    required which makes it appear more like a reimplementation.

    A strong focus was on splitting state that is maintained on the
    application's thread (with Stream) and state that is maintained by the
    transport (and used for StreamListener). By splitting the state it makes
    it much easier to verify thread-safety and to reason about interactions.

    I consider this a stepping stone for making even more changes to
    simplify the Stream implementations and do not think some of the changes
    are yet at their logical conclusion. Some of the changes may also
    immediately be replaced with something better. The focus was to improve
    readability and comprehesibility to more easily make more interesting
    changes.

    The only thing really removed is some state checking during sending
    which is already occurring in ServerCallImpl.