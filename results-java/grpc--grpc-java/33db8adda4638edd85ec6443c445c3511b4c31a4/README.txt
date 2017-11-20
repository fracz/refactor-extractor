commit 33db8adda4638edd85ec6443c445c3511b4c31a4
Author: Eric Anderson <ejona@google.com>
Date:   Fri Dec 4 12:11:53 2015 -0800

    Remove outdated and misleading JavaDoc

    "onHeaders always is called" is from the initial gRPC design where
    0-many context frames were sent before the first message. That is why it
    was possible for "no headers were received". That has long-since not
    been true, but in the various refactorings this language was
    accidentally left. The language "Headers always precede messages" is
    correct since headers are only guaranteed if messages were sent.