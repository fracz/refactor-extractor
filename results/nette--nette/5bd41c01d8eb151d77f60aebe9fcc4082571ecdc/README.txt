commit 5bd41c01d8eb151d77f60aebe9fcc4082571ecdc
Author: Miloslav Hůla <miloslav.hula@gmail.com>
Date:   Wed Mar 21 11:31:57 2012 +0100

    Database: fixed PgSqlDriver

    Reflection code is refactored. information_schema is used as more as
    possible for compatibility. Only getIndexes() use PG catalog, because in
    information_schema are not all indexes.