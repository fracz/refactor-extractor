commit 3bcf5e2a46a612f98a3a5eda4f30fad4d57559a5
Author: SteveJobzniak <SteveJobzniak@users.noreply.github.com>
Date:   Mon Apr 17 19:21:44 2017 +0200

    Huge speed improvements for video uploading

    As most of you are aware, Instagram's servers are overloaded during peak
    traffic hours, which means they randomly drop uploaded video chunks.

    Previously, we retried the whole video upload from scratch every time
    they had dropped some chunk(s).

    But thanks to this massive rewrite, the uploader is now able to
    re-upload JUST the individual chunks/byte ranges that have been lost!

    As a result, video uploads during peak hours should now be about 5x faster!

    Meanwhile, PHP memory usage will still be as low as before, because
    we're only reading 1 chunk into memory at a time (even during retries).

    :-)