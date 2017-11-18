commit 4b81dfb7c1946805a25970d004ccc4d330af064b
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Tue Jan 24 14:05:43 2017 +0300

    JS: improve binary representation format of metadata in .meta.js

    Instead of writing many different files and serializing this "virtual file
    system" to a byte array in a protobuf message, just write the needed stuff
    directly, as fields in the Library message. Make it consist of many Part
    messages, where the Part message is equivalent to the BuiltIns message in
    builtins.proto. The next step would be to combine Library.Part and BuiltIns,
    which will allow us to simplify some serialization-related code soon.

    In this commit, no changes happened to the .kjsm format. But since the code
    that serialized the abovementioned files was shared, a temporary abstraction
    over two serialization formats was made, see SerializerCallbacks.

    This commit temporarily breaks .kjsm decompiler and stub builder