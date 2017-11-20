commit b57d9b9f4ef84a1901709404a2e578d4e3861c14
Author: lvca <l.garulli@gmail.com>
Date:   Fri Aug 19 18:57:58 2016 -0500

    HA: improved management of contention by using database interface instead of pure storage

    This allows to always call the right hooks (like indexes)