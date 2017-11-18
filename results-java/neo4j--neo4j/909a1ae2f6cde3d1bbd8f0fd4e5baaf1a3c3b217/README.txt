commit 909a1ae2f6cde3d1bbd8f0fd4e5baaf1a3c3b217
Author: Chris Leishman <chris@leishman.org>
Date:   Fri Aug 21 16:11:57 2015 -0700

    Move abstract NeoServer init out of constructor

    Ideally this would instead be refactored to remove the heirarchy and use
    dependency injection, however that'll have to wait until the API can be
    changed.