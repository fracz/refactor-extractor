commit 28cb56efa285f63b29dd169b7740330277470168
Author: astroshim <hsshim@zepl.com>
Date:   Sun Dec 11 00:09:15 2016 +0900

    Better output of JDBC Interpreter.

    ### What is this PR for?
    Currently, the output format of the JDBC Interpreter is the table format with all the results except EXPLAIN statement, It is better for the user experience to output only the result of the SELECT statement in table format.
    This PR fixes the above issue and did some code refactoring.
    Tested on mysql and aws athena.

    ### What type of PR is it?
    Improvement

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1784

    ### How should this be tested?
     - before
    <img width="1132" alt="2016-12-11 12 30 54" src="https://cloud.githubusercontent.com/assets/3348133/21074311/3bb25bd0-bf39-11e6-8cc2-c72af742c6fe.png">

    <img width="1105" alt="2016-12-11 12 32 47" src="https://cloud.githubusercontent.com/assets/3348133/21074320/61b2cdd8-bf39-11e6-8be2-b02eb48b32a7.png">

     - after
    <img width="1147" alt="2016-12-11 12 15 43" src="https://cloud.githubusercontent.com/assets/3348133/21074296/e349e3e6-bf38-11e6-96c2-fa7e92265e94.png">

    <img width="1097" alt="2016-12-11 12 21 06" src="https://cloud.githubusercontent.com/assets/3348133/21074297/e888130a-bf38-11e6-963e-d9fbe7eafb0f.png">

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: astroshim <hsshim@zepl.com>

    Closes #1744 from astroshim/ZEPPELIN-1784 and squashes the following commits:

    884113c [astroshim] change output type of JDBC Interpreter.