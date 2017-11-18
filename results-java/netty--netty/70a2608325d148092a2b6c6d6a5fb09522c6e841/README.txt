commit 70a2608325d148092a2b6c6d6a5fb09522c6e841
Author: nmittler <nathanmittler@google.com>
Date:   Mon Apr 6 12:55:20 2015 -0700

    Optimizing user-defined stream properties.

    Motivation:

    Streams currently maintain a hash map of user-defined properties, which has been shown to add significant memory overhead as well as being a performance bottleneck for lookup of frequently used properties.

    Modifications:

    Modifying the connection/stream to use an array as the storage of user-defined properties, indexed by the class that identifies the index into the array where the property is stored.

    Result:

    Stream processing performance should be improved.