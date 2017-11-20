commit 49fd4dd85b37da2eaffd3ed10ace1ae4dd99219b
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Fri Dec 3 16:58:51 2010 +0000

    Improved mode saveOnlyDirty in ODatabaseObjectTx: saves only the objects market as dirty. This improve a lot when you have a huge graph but requires to set dirty by hand, probably at every setter method.