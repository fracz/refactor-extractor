commit bc5f414bf597fe440ca096333fafea330746a323
Author: Steve Clay <steve@mrclay.org>
Date:   Tue Sep 6 09:05:57 2016 -0400

    fix(pages): operations keep track of more than 10 child pages

    Sets `0` limit on queries for child queries, converts to batches, some
    small refactoring for clarity.