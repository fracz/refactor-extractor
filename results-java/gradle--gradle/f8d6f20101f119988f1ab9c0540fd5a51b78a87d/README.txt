commit f8d6f20101f119988f1ab9c0540fd5a51b78a87d
Author: daz <daz@bigdaz.com>
Date:   Tue Feb 26 09:46:32 2013 -0700

    More refactoring simplification of imported ivy code
    - Remove code for handling conflict-mediators and dependency-overrides, since they are ignored by Gradle anyway.
    - Centralised use of ParserSettings