commit 388d85fb5d3d8deff0ad0719222ef4d3ac76f6e4
Author: Havoc Pennington <hp@pobox.com>
Date:   Thu Apr 12 13:04:14 2012 -0400

    Serialization-pocalypse: change serialization format

    The previous use of Java's default serialization dumped
    all implementation-detail class names and fields into the serialization,
    making it basically impossible to improve the implementation.

    Two strategies here:
     - prohibit serialization of unresolved configs, which are
       the location of a lot of implementation detail
     - delegate all serialization to an Externalizable
       SerializedConfigValue class, which serializes
       using fields that have lengths. Unknown fields
       can thus be skipped and we can write code to
       support obsolete fields, and so on.

    As a side effect, this makes the serialization far more compact
    because we don't need the Java per-class header noise, and we
    jump through some hoops to avoid writing out duplicate ConfigOrigin
    information. It still isn't super-compact compared to something
    like protobuf but it's a lot less insane.