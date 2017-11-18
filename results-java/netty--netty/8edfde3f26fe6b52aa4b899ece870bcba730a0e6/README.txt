commit 8edfde3f26fe6b52aa4b899ece870bcba730a0e6
Author: Norman Maurer <nmaurer@redhat.com>
Date:   Fri Jul 11 08:31:39 2014 +0200

    [#2650] Allow to disable http header validation in SpdyHttpDecoder and SpdyHttpCodec

    Motivation:

    HTTP header validation can be expensive so we should allow to disable it like we do in HttpObjectDecoder.

    Modification:

    Add constructor argument to disable validation.

    Result:
    Performance improvement