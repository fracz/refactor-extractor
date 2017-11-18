commit 9ea97b5ed91278fb8ddcd7c2f6d7665430225db8
Author: Tomasz Bak <tbak@netflix.com>
Date:   Thu Mar 19 12:41:31 2015 -0700

    Use Jackson JSON serializer instead of XStream to improve serialization
    performance/memory usage.

    XML serialization is still done by XStream.