commit 8a75ba35ef0908529bccd85e5c08bdb6f5fb04bc
Author: Norman Maurer <nmaurer@redhat.com>
Date:   Wed Jun 25 15:01:57 2014 +0200

    [#2599] Not use sun.nio.ch.DirectBuffer as it not exists on android

    Motivation:

    During some refactoring we changed PlatformDependend0 to use sun.nio.ch.DirectBuffer for release direct buffers. This broke support for android as the class does not exist there and so an exception is thrown.

    Modification:

    Use again the fieldoffset to get access to Cleaner for release direct buffers.

    Result:
    Netty can be used on android again