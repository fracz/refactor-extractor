commit 94cd34203802a3ed415456f8996a66648bc785cd
Author: Oliver Gierke <ogierke@pivotal.io>
Date:   Sun Jun 14 11:55:52 2015 +0200

    DATAJPA-736 - Added support for non-ASCII characters in entity names.

    We now use an improved regular expression to detect identifier clauses in JPQL queries to be able to derive count queries for queries that use non-ASCII characters in entity names.