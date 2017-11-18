commit b9348bb89c6116c6c57b68c34b2ff28b5b3d4ce3
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Mon Sep 22 14:02:35 2014 +0200

    Fix Protobuf support - HTTP headers already written

    Prior to this commit, the `ProtobufHttpMessageConverter` would call
    `outputMessage.getBody()` at the beginning of the `writeInternal`
    method, thus writing HTTP headers. Since this method is trying to write
    "x-protobuf" headers after that, protobuf support wasn't working
    properly for the default "x-protobuf" media type.

    Thanks Toshiaki Maki for the repro project!

    Also fixed:
    * improve `MockHttpOutputMessage` behavior to reproduce the read-only
    state of HTTP headers once they've been written.

    Issue: SPR-12229