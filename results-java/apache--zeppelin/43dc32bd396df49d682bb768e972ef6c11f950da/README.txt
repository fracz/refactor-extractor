commit 43dc32bd396df49d682bb768e972ef6c11f950da
Author: astroshim <hsshim@nflabs.com>
Date:   Tue Aug 16 14:39:51 2016 +0900

    [Zeppelin-945] Interpreter authorization

    ### What is this PR for?
    This PR is derived from https://github.com/apache/zeppelin/pull/1223

    ### What type of PR is it?
    Improvement

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-945

    ### How should this be tested?
    You can change user permission of interpreter at interpreter setting page.

    ### Screenshots (if appropriate)
    ![result](https://cloud.githubusercontent.com/assets/3348133/17457867/904e2754-5c3e-11e6-948c-b2969c4b89c7.gif)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: astroshim <hsshim@nflabs.com>

    Closes #1257 from astroshim/ZEPPELIN-945_1 and squashes the following commits:

    83097ab [astroshim] fix naming.
    2c48ded [astroshim] code refactor.
    7ed8ad6 [astroshim] fix js code style
    3e25159 [astroshim] move directive to componets
    f07542a [astroshim] remove comment
    febce0c [astroshim] fix js
    e42cb9e [astroshim] fix initialize user value
    e1e7a35 [astroshim] add interpreter create.
    e72c097 [astroshim] Merge branch 'master' into ZEPPELIN-945_1
    e1679b3 [astroshim] wip
    e56e5b1 [astroshim] wip
    635d523 [astroshim] Merge branch 'master' of https://github.com/apache/zeppelin into ZEPPELIN-945_1
    1ae1c6a [astroshim] fix for @AhyoungRyu pointed.
    b34fdf7 [astroshim] fix for checking intpname
    29d8f43 [astroshim] fix js
    407d260 [astroshim] fit UX