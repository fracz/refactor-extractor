commit 51d61445915e0a607742a46c60332e015f9559b6
Author: Moxie Marlinspike <moxie@thoughtcrime.org>
Date:   Mon May 8 15:32:59 2017 -0700

    Significant MMS changes

    1) Remove all our PDU code and switch to the PDU code from the
       klinker library

    2) Switch to using the system Lollipop MMS library by default,
       and falling back to our own custom library if that fails.

    3) Format SMIL differently, using code from klinker instead of
       what we've pieced together.

    4) Pull per-carrier MMS media constraints from the XML config
       files in the klinker library, instead of hardcoding it at 280kb.

    Hopefully this is an improvement, but given that MMS is involved,
    it will probably make things worse instead.