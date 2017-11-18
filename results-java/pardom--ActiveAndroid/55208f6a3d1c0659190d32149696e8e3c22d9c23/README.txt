commit 55208f6a3d1c0659190d32149696e8e3c22d9c23
Author: Markus Pfeiffer <m.pfeiffer@mwaysolutions.com>
Date:   Wed Apr 16 13:01:38 2014 +0200

    Added a new SQL parser. This parser supports both SQL style line comments and comment blocks. SQL statements can span multiple lines but must be delimited with a semicolon. This allows for improved readability of SQL scripts.

    The old parser expects one statement per line which may or may not end with a semicolon. This means the new parser can also process scripts written for the old parser if those scripts end each line with a semicolon.

    For backwards compatibility reasons this parser isn't used unless the manifest contains a meta-data entry "AA_SQL_PARSER" with the value "delimited". If this value isn't specified or is set to "legacy" the old parser implementation is used.