commit 55cb6b894b1a62e4e941e63abaad834d15ca733e
Author: Benoy Antony <benoy@apache.org>
Date:   Thu Apr 27 23:51:48 2017 -0700

    [ZEPPELIN-2465] Minor code fixes for the livy package

    ### What is this PR for?
    Minor code fixes for the livy package.
    The code fixes include :
    Fixing a typo in a classname - BaseLivyInterprereter to BaseLivyInterpreter
    Removing an unused variable in BaseLivyInterpreter
    Removing unused imports in a few classes

    ### What type of PR is it?
    Refactoring

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-2465

    ### How should this be tested?
    No need to test as there is no change in funcionality

    ### Questions:
    * Does the licenses files need update? NO
    * Is there breaking changes for older versions? NO
    * Does this needs documentation? NO

    Author: Benoy Antony <benoy@apache.org>

    Closes #2297 from benoyantony/refactor and squashes the following commits:

    bd6d8ff [Benoy Antony] ZEPPELIN-2465 Minor code fixes for the livy package