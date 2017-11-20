commit fdbfff2090accb032949551348ff4ca29e8de73b
Author: soralee <sora0728@zepl.com>
Date:   Thu Jun 29 17:30:06 2017 +0900

    [ZEPPELIN-2661]Test: personalized mode action

    ### What is this PR for?
    Adding to personalized mode action case on Selenium test.
    Here is for checking scenario.
    #### 1. Simple action test :
    ```
    1. (admin) login, create a new note, run a paragraph with 'Before' text, turn on personalized mode, logout.
               > checkpoint : result of paragraph is 'Before'.

    2. (user1) login, make sure it is on personalized mode and 'Before' in result of paragraph, logout.
               > checkpoint 1 : enabling personalized mode
               > checkpoint 2 : result of paragraph is 'Before'.

    3. (admin) login, run after changing paragraph contents to 'After', check result of paragraph, logout.
               > checkpoint : changed from 'Before' to 'After' in paragraph result.

    4. (user1) login, check whether result of paragraph is 'Before' or not.
               > checkpoint : result of paragraph is 'Before', 'After' means test fail.
    ```

    #### 2. Graph action test :
    ```
    1. (admin) login, create a new note, run a paragraph with data of spark tutorial, change active graph to 'Bar Chart', turn on personalized mode, logout.
               > checkpoint 1: result string of paragraph contains import 'org.apache.commons.io.IOUtils'.
               > checkpoint 2 : check active graph is 'Bar Chart'

    2. (user1) make sure it is on personalized mode and graph mode is on 'Bar Chart', try to change active graph to 'Table' and then check result
               > checkpoint 1 : enabling personalized mode
               > checkpoint 2 : final result of active graph is 'Bar Chart', 'Table' means test fail.
    ```

    #### 3. Dynamic form action test :
    ```
    1. (admin) login, create a new note, run a paragraph with data of spark tutorial, logout.
               > checkpoint 1: result string of paragraph contains import 'org.apache.commons.io.IOUtils'.
               > checkpoint 2 : check a dynamic form value is '30'

    2. (user1) make sure it is on personalized mode and  dynamic form value is '30', try to change dynamic form value to '10' and then check result
               > checkpoint 1 : enabling personalized mode
               > checkpoint 2 : final result of dynamic form value is '30', '10' means test fail.
    ```

    ### What type of PR is it?
    [ Test ]

    ### What is the Jira issue?
    * [ZEPPELIN-2661](https://issues.apache.org/jira/browse/ZEPPELIN-2661)

    ### How should this be tested?
    - build zeppelin with `mvn clean package -DskipTests'

     -  1. execute for Simple text test : `TEST_SELENIUM="true" mvn test -pl 'zeppelin-server' --am -DfailIfNoTests=false -Dtest=PersonalizeActionsIT#testSimpleAction`

     - 2. execute for Graph test : `TEST_SELENIUM="true" mvn test -pl 'zeppelin-server' --am -DfailIfNoTests=false -Dtest=PersonalizeActionsIT#testGraphAction`

     - 3. execute for Graph test : `TEST_SELENIUM="true" mvn test -pl 'zeppelin-server' --am -DfailIfNoTests=false -Dtest=PersonalizeActionsIT#testDynamicFormAction`

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: soralee <sora0728@zepl.com>

    Closes #2415 from soralee/personalize_mode_test and squashes the following commits:

    f2d14d9b [soralee] missing endToEndTestEnabled method
    573a3105 [soralee] fix: remove annotation
    0d55b765 [soralee] fix: reuse code in AuthenticationIT login and logout
    a322427f [soralee] fix: null exception in Travis
    970f6b2b [soralee] fix: Travis fail this line because of null
    a2b3c288 [soralee] fix: flaky test in login section on Travis
    1844159e [soralee] fix: remove unnecessary code
    90cb1982 [soralee] fix: improve simple code in paragraph of graph and dynamicform test case
    84ec218d [soralee] fix: passing overall test and flaky test in login section
    d347fd1d [soralee] Test&Fix: adding dynamic form action & code improvement
    dd7cb246 [soralee] test&fix: adding graph action test & remove duplicated code
    2581af1a [soralee] Fix: remove unnecessary newline and annotation
    043e8387 [soralee] fix: using minimum 'sleep' method
    ecac3c36 [soralee] style: modify method name
    cb281a89 [soralee] style: [minor] modify annotation
    8e13fad0 [soralee] style: added new line
    49a339a8 [soralee] test: add to check point about being turned on personalized mode in user1
    ad77924a [soralee] personalize mode integration test