commit 8561929164846ef4b3323a8b3f88a09ea1441785
Author: Vedran Pavic <vedran.pavic@gmail.com>
Date:   Tue Sep 19 21:28:12 2017 +0200

    Improve Spring Session sample

    This commit improves the Spring Session sample by providing multiple
    build profiles that make it possible to easily try out different session
    stores. By default, JDBC session store backed by an in-memory embedded H2
    database is used.

    See gh-10351