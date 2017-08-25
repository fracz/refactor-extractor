commit c32bfef96ea996c8673f9769f845a70aaa3ef317
Author: Ismayil Khayredinov <ismayil.khayredinov@gmail.com>
Date:   Fri Aug 19 10:25:53 2016 +0200

    chore(entities): refactor users and entity tables

    Refactors database queries in entity and user tables
    Moves memcache logic into entity cache service
    Improves cache flushing logic