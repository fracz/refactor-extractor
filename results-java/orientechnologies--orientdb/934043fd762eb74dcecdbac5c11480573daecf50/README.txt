commit 934043fd762eb74dcecdbac5c11480573daecf50
Author: Roberto Franchini <ro.franchini@gmail.com>
Date:   Tue Apr 5 12:13:05 2016 +0200

    improves exception mapping

    https://github.com/orientechnologies/orientdb/issues/5922

    Underlying OrientDB specific code throws unchecked OException and derivates. These exceptions are now catched and mapped to SQLException, as defined in the JDBC contract