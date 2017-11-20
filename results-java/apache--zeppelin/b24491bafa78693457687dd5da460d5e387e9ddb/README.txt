commit b24491bafa78693457687dd5da460d5e387e9ddb
Author: astroshim <hsshim@nflabs.com>
Date:   Fri Sep 23 16:31:19 2016 +0900

    [ZEPPELIN-1405] ConnectionPool for JDBCInterpreter.

    ### What is this PR for?
    This PR is for refactoring code for JDBCInterpreter.
    There is no putting 'Connection' to 'propertyKeyUnusedConnectionListMap' anywhere in the original code.

    ### What type of PR is it?
    Improvement

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1405

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: astroshim <hsshim@nflabs.com>

    Closes #1396 from astroshim/ZEPPELIN-1405 and squashes the following commits:

    b07e162 [astroshim] add checking connection is null
    f6998c2 [astroshim] Merge branch 'master' into ZEPPELIN-1405
    1862ae6 [astroshim] Merge branch 'master' into ZEPPELIN-1405
    efc2bfc [astroshim] rebase
    21217a7 [astroshim] fix indentation.
    4d4f85c [astroshim] refactoring code of close()
    9f1e368 [astroshim] replace ConnectionPool
    4dabbcc [astroshim] wip) changing to use dbcp
    12dd7cb [astroshim] remove propertyKeyUnusedConnectionListMap map