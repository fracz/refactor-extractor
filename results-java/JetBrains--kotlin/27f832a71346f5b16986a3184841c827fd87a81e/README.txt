commit 27f832a71346f5b16986a3184841c827fd87a81e
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Mon Nov 11 20:41:20 2013 +0400

    BodiesResolveContext.getClasses()'s key is JetClassOrObject

    This has no effect right now, but is a preparation for the objects refactoring,
    when BodiesResolveContext.getObjects() will be dropped