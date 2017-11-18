commit 10f80167e66901ce9b208b974d4d8ac2bdc6fa53
Author: javanna <cavannaluca@gmail.com>
Date:   Fri Jun 26 15:38:11 2015 +0200

    Query refactoring: validate inner queries whenever supported

    Added validation for all inner queries that any already refactored query may hold. Added also tests around this. At the end of the refactoring validate will be called by SearchRequest#validate during TransportSearchAction execution, which will call validate against the top level query builder that will need to go and validate its data plus all of its inner queries, and so on.

    Closes #11889