commit 2ff4e344e9c661763eabc68b8b33692b08a40932
Author: Steve Clay <steve@mrclay.org>
Date:   Mon Feb 27 16:25:41 2017 -0500

    chore(river): warn devs that elgg_get_river() has no "views" option

    Devs who refactor `elgg_delete_river()` code to use `elgg_get_river()`
    may be surprised that it doesn't support this option.

    Fixes #10791