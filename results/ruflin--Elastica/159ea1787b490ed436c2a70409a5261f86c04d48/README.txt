commit 159ea1787b490ed436c2a70409a5261f86c04d48
Author: Nik Everett <neverett@wikimedia.org>
Date:   Mon Mar 3 15:01:38 2014 -0500

    Improve performance of Status->getIndicesWithAlias

    Particularly improves performance on clusters with many indices.  Slow
    to instant.

    Closes #563