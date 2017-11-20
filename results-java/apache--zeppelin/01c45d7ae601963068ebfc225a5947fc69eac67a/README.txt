commit 01c45d7ae601963068ebfc225a5947fc69eac67a
Author: astroshim <hsshim@nflabs.com>
Date:   Wed Nov 30 00:35:23 2016 +0900

    [ZEPPELIN-1724] conda run command removed in 4.1.0

    ### What is this PR for?
    Because `conda run` command removed since version `4.0.9`, PythonCondaInterpreter not working after the `conda-4.0.9`.
    This PR fixes this issue.

    I tested conda-4.2.12 and conda-4.0.9 .

    ### What type of PR is it?
    Bug Fix | Improvement

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1724

    ### How should this be tested?
    Please refer to https://github.com/apache/zeppelin/pull/1645

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: astroshim <hsshim@nflabs.com>

    Closes #1699 from astroshim/ZEPPELIN-1724 and squashes the following commits:

    294b6f9 [astroshim] refactoring and fix testcase
    8c3fbd3 [astroshim] fix conda version