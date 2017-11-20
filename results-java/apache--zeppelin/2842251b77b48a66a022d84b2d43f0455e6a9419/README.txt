commit 2842251b77b48a66a022d84b2d43f0455e6a9419
Author: soralee <sora0728@zepl.com>
Date:   Fri Jun 30 10:20:50 2017 +0900

    [ZEPPELIN-2693] Test: interpreter mode action test

    ### What is this PR for?
    Adding to interpreter mode (globally shared mode, Per user/Scoped mode, Per user/isolated mode) action case on Selenium test.
    Here is for checking scenario.

    #### 1.  globally in shared mode
    ```
    admin : set 'globally in shared' mode of python interpreter

    User 1: create new note
    User 1: Code “%python user = user1” in the first paragraph
    User 1: Code “%python print user” in the second paragraph and run it
    User 1: Check if the result is user1 in the second paragraph
    System: Check if the number of python interpreter process is 1
    System: Check if the number of python process is 1

    User 2: create new note
    User 2: Code “%python user = user2” in the first paragraph
    User 2: Code “%python print user” in the second paragraph and run it
    User 2: Check if the result is user2 in the second paragraph
    System: Check if the number of python interpreter process is 1
    System: Check if the number of python process is 1

    User 1: Run the second paragraph again.
    User 1: Check if the result is user2 in the second paragraph
    User 1: Restart python interpreter in the note.
    System: Check if the number of python interpreter and python process is 0
    ```

    #### 2. Per user in scoped mode
    ```
    admin : set 'Per user in scoped' mode of python interpreter

    User 1: create new note
    User 1: Code “%python user = user1” in the first paragraph
    User 1: Code “%python print user” in the second paragraph and run it
    User 1: Check if the result is user1 in the second paragraph
    System: Check if the number of python interpreter process is 1
    System: Check if the number of python process is 1

    User 2: create new note
    User 2: Code “%python user = user2” in the first paragraph
    User 2: Code “%python print user” in the second paragraph and run it
    User 2: Check if the result is user2 in the second paragraph
    System: Check if the number of python interpreter process is 1
    System: Check if the number of python process is 2

    User 1: Run the second paragraph again.
    User 1: Check if the result is user1 in the second paragraph
    User 1: Restart python interpreter in the note.
    System: Check if the number of python interpreter process is 1
    System: Check if the number of python process is 1

    User 2: Restart python interpreter in the note.
    System: Check if the number of python interpreter process is 0
    System: Check if the number of python process is 0

    User 1: Run the first paragraph
    User 2: Run the first paragraph
    System: Check if the number of python interpreter process is 1
    System: Check if the number of python process is 2

    admin: Restart python interpreter in interpreter tab
    System: Check if the number of python interpreter process is 0
    System: Check if the number of python process is 0
    ```

    #### 2. Per user in isolated mode
    ```
    admin : set 'Per user in isolated' mode of python interpreter

    User 1: create new note
    User 1: Code “%python user = user1” in the first paragraph
    User 1: Code “%python print user” in the second paragraph and run it
    User 1: Check if the result is user1 in the second paragraph
    System: Check if the number of python interpreter process is 1
    System: Check if the number of python process is 1

    User 2: create new note
    User 2: Code “%python user = user2” in the first paragraph
    User 2: Code “%python print user" in the second paragraph and run it
    User 2: Check if the result is user 2 in the second paragraph
    System: Check if the number of python interpreter process is 2
    System: Check if the number of python process is 2

    User 1: Run the second paragraph again.
    User 1: Check if the result is user1 in the second paragraph
    User 1: Restart python interpreter in the note.
    System: Check if the number of python interpreter process is 1
    System: Check if the number of python process is 1

    User 2: Restart python interpreter in the note.
    System: Check if the number of python interpreter process is 0
    System: Check if the number of python process is 0

    User 1: Run the first paragraph
    User 2: Run the first paragraph
    System: Check if the number of python interpreter process is 2
    System: Check if the number of python process is 2

    admin : Restart python interpreter in interpreter tab
    System: Check if the number of python interpreter process is 0
    System: Check if the number of python process is 0
    ```

    ### What type of PR is it?
    [ Test ]

    ### Todos
     - * [x] - add logic to check process - interpreter process and python process
     - * [x] - add logic to restart python interpreter button in note
     - * [x] - add logic to restart python interpreter in interpreter tab

    1. * [x] - update and complete `Globally in shared mode` scenario
    2. * [x] - make and complete `Per user in scoped mode` scenario
    3. * [x] - make and complete `Per user in isolated mode` scenario

    ### What is the Jira issue?
    * [adding Interpreter mode test](https://issues.apache.org/jira/browse/ZEPPELIN-2693)

    ### How should this be tested?

    1. build zeppelin with `mvn clean package -DskipTests`

    2.1. total test : run `TEST_SELENIUM='true' mvn test -pl 'zeppelin-server' --am -DfailIfNoTests=false -Dtest=InterpreterModeActionsIT`

    2.2.  globally mode -
    `TEST_SELENIUM='true' mvn test -pl 'zeppelin-server' --am -DfailIfNoTests=false -Dtest=InterpreterModeActionsIT#testGloballyAction`

    2.3.  Per user in scoped mode -
    `TEST_SELENIUM='true' mvn test -pl 'zeppelin-server' --am -DfailIfNoTests=false -Dtest=InterpreterModeActionsIT#testPerUserScopedAction`

    2.4. Per user in isolated mode -
    `TEST_SELENIUM='true' mvn test -pl 'zeppelin-server' --am -DfailIfNoTests=false -Dtest=InterpreterModeActionsIT#testPerUserIsolatedAction`

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: soralee <sora0728@zepl.com>

    Closes #2438 from soralee/ZEPPELIN-2693_adding_Interpreter_mode_test and squashes the following commits:

    1bca7f4e [soralee] fix: fix typo
    40ecbe91 [soralee] fix: complete 3.Per user in isolated mode scenario
    38e27f38 [soralee] fix: improve code
    52aa05a2 [soralee] fix: complete 2.Per user in scoped mode scenario
    edb41a72 [soralee] fix: complete 1.globally in shared mode scenario
    b3e3bf93 [soralee] fix: add permission to user1 and user2, add todo annotation
    8a1e8116 [soralee] Test: adding initial interpreter mode action test