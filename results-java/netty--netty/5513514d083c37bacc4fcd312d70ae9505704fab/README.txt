commit 5513514d083c37bacc4fcd312d70ae9505704fab
Author: Norman Maurer <norman_maurer@apple.com>
Date:   Mon Aug 1 21:09:48 2016 +0200

    Take readerIndex into account when write to BIO.

    Motivation:

    We should take the readerIndex into account whe write into the BIO. Its currently not a problem as we slice before and so the readerIndex is always 0 but we should better not depend on this as this will break easily if we ever refactor the code and not slice anymore.

    Modifications:

    Take readerIndex into acount.

    Result:

    More safe and correct use.