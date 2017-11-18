commit e08ed6ae53bcce54965573138ccd63a84ba4abc5
Author: Andrii Rosa <Andriy.Rosa@TERADATA.COM>
Date:   Wed Apr 26 12:20:10 2017 -0400

    Refactor CassandraRecordSink

    Build query with query builder provided by the DataStax driver.
    Use PreparedStatement to perform inserts.
    This will ensure proper column and table names escape, and improve
    performance.