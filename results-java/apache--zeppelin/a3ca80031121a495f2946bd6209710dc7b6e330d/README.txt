commit a3ca80031121a495f2946bd6209710dc7b6e330d
Author: Mina Lee <minalee@apache.org>
Date:   Mon Sep 19 19:05:39 2016 +0200

    [ZEPPELIN-1026] set syntax highlight based on default bound interpreter

    ### What is this PR for?
    This is complete work of #1148. Comments and tasks on #1148 has been handled in this PR.
    - Add syntax language information in `interpreter-setting.json`
    - When user type `%replName` in paragraph, back-end check if the interpreter name with `replName` exists, and return language information to front-end if it does
    - If user doesn't specify `%replName`, default interpreter's language will be used
    - Using alias name for paragraph syntax highlight

    ### What type of PR is it?
    [Bug Fix | Improvement]

    ### What is the Jira issue?
    [ZEPPELIN-1026](https://issues.apache.org/jira/browse/ZEPPELIN-1026)

    ### How should this be tested?
    1. Create new note and make markdown interpreter to be default.
    2. See if markdown syntax is applied.

    ### Screenshots (if appropriate)
    #### Case 1. When the default interpreter set to python interpreter.
    **Before**
    Has `scala` as syntax highlight language when %python is not set.
    <img width="665" alt="screen shot 2016-07-07 at 10 46 20 pm" src="https://cloud.githubusercontent.com/assets/8503346/16655312/af67a302-4494-11e6-949e-793ad0515d7a.png">

    **After**
    Has `python` as syntax highlight language even when %python is not set.
    <img width="666" alt="screen shot 2016-07-07 at 10 44 39 pm" src="https://cloud.githubusercontent.com/assets/8503346/16655248/769d8ba4-4494-11e6-9b3c-dc5e026e9c53.png">

    #### Case 2. When use alias name as repl name.
    **Before**
    <img width="742" alt="screen shot 2016-09-08 at 4 22 39 pm" src="https://cloud.githubusercontent.com/assets/8503346/18353471/620c5ede-75e2-11e6-9d01-0726bc900dc0.png">

    **After**
    <img width="741" alt="screen shot 2016-09-08 at 4 34 57 pm" src="https://cloud.githubusercontent.com/assets/8503346/18353487/6cdaa406-75e2-11e6-831a-08e0fa3a85d8.png">

    ### Further possible improvements
    There are still several cases that Zeppelin doesn't handle syntax highlight well. These can be handled with another jira ticket/PR.
    1. When default bound interpreter changes, syntax highlight is not changed accordingly
    2. When copy/paste code, syntax highlight won't be applied properly since Zeppelin only checks changes when cursor is in first line.

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? yes(for creating new interpreter)

    Author: Mina Lee <minalee@apache.org>

    Closes #1415 from minahlee/ZEPPELIN-1026 and squashes the following commits:

    c66fb0e [Mina Lee] Move getEditorSetting to InterpreterFactory class
    2d56222 [Mina Lee] Add description about default syntax highlight in doc
    08ccad9 [Mina Lee] Fix test
    0874522 [Mina Lee] Change condition for triggering 'getAndSetEditorSetting' to reduce front-end <-> back-end communication
    9e4f2e9 [Mina Lee] Change the way to read interpreter language from interpreter-setting.json after #1145
    75543b3 [Mina Lee] Add test
    565d9d0 [Mina Lee] [DOC] Setting syntax highlight when writing new interpreter
    20132ca [Mina Lee] Get paragraph editor mode from backend
    52f4207 [Mina Lee] Align comments for readability
    26cbbb8 [Mina Lee] Add editor field