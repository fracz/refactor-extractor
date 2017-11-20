commit e33be2525679982a5eae4cb61e7ec267262effdf
Author: 1ambda <1amb4a@gmail.com>
Date:   Fri Nov 18 22:51:52 2016 +0900

    [ZEPPELIN-1684] Add GET /interpreter/setting/id REST API

    ### What is this PR for?

    Due to the lack of this missing API, some front-end code retrieve all settings. This is inefficient.
    We can modify those codes by adding this API.

    Also, i refactored the whole `InterpreterRestApiTest`

    ### What type of PR is it?
    [Feature | Documentation | Refactoring]

    ### What is the Jira issue?
    [ZEPPELIN-1684](https://issues.apache.org/jira/browse/ZEPPELIN-1684)

    ### How should this be tested?

    You can run unit test `InterpreterRestApiTest`

    ### Screenshots (if appropriate)

    Updated doc screenshots

    <img width="900" alt="new_api_doc" src="https://cloud.githubusercontent.com/assets/4968473/20423419/74eeb2a0-adb3-11e6-9a69-58e33457d514.png">

    ### Questions:
    * Does the licenses files need update? - NO
    * Is there breaking changes for older versions? - NO
    * Does this needs documentation? - YES, but done.

    Author: 1ambda <1amb4a@gmail.com>

    Closes #1655 from 1ambda/feat/create-interpreter-get-api and squashes the following commits:

    89ff7fd [1ambda] style: Apply zeppelin checkstyle xml
    c901287 [1ambda] docs: Add new api to rest-interpreter.html
    2641a73 [1ambda] refactor: InterpreterRestApiTest
    e63c80e [1ambda] refactor: testSettingsCRUD
    9bc24fd [1ambda] feat: Add /interpreter/setting/id API and test