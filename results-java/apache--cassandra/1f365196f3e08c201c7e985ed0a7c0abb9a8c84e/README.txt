commit 1f365196f3e08c201c7e985ed0a7c0abb9a8c84e
Author: Eric Evans <eevans@apache.org>
Date:   Fri Sep 14 10:08:24 2012 -0500

    p/4443/010_refactor_range_move

    Break out common code from SS.move(Token) for later use in range
    relocation.

    Patch by eevans; reviewed by Brandon Williams for CASSANDRA-4559