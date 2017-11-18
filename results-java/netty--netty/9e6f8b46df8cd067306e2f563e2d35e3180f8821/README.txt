commit 9e6f8b46df8cd067306e2f563e2d35e3180f8821
Author: Trustin Lee <trustin@gmail.com>
Date:   Wed May 2 15:01:58 2012 +0900

    Retrofit the NIO transport with the new API / improve the new API

    - Remove the classes and properties that are not necessary anymore
    - Remove SingleThreadEventLoop.newRegistrationTask() and let
      Channel.Unsafe handle registration by itself
    - Channel.Unsafe.localAddress() and remoteAddress()
      - JdkChannel is replaced by Channel.Unsafe.