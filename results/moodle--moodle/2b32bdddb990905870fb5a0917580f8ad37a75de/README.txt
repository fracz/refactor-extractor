commit 2b32bdddb990905870fb5a0917580f8ad37a75de
Author: moodler <moodler>
Date:   Thu Jul 3 12:19:54 2003 +0000

    Keep strings cached in memory during a single page to improve performance
    on slow filesystems or bad file caching.   (experimental)