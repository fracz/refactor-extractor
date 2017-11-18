commit 088508d8bf02eec0cac6e0e3fea7ce2c74293798
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Fri May 16 11:01:07 2014 +0200

    Unified batch friendly window pool factory

    and some minor improvements to stats and toString() on InputEntities. Also
    abstracted available memory calculations into its own class and has
    LongArrayFactory.AUTO use that. So there are now mock tests around that
    calculation and LongArray allocation.