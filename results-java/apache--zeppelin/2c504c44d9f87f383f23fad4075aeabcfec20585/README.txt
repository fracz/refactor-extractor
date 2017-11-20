commit 2c504c44d9f87f383f23fad4075aeabcfec20585
Author: Tinkoff DWH <tinkoff.dwh@gmail.com>
Date:   Mon Apr 24 12:18:55 2017 +0500

    [ZEPPELIN-2216] general solution to precode. refactoring jdbc precode

    ### What is this PR for?
    General solution to execute precode. Refactoring jdbc precode using general solution. Task contains to subtasks: executeAfterOpen, executeBeforeClose. executeBeforeClose not done because we need the context so there is a solution only for executeAfterOpen.

    ### What type of PR is it?
    Feature | Refactoring

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-2216

    ### How should this be tested?
    1. Add parameter zeppelin.PySparkInterpreter.precode `someVar='text'`
    2. Execute
    ```
    %pyspark
    print(someVar)
    ```

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: Tinkoff DWH <tinkoff.dwh@gmail.com>

    Closes #2221 from tinkoff-dwh/ZEPPELIN-2216 and squashes the following commits:

    1e3f3f7 [Tinkoff DWH] [ZEPPELIN-2216] fix path
    e4cf72f [Tinkoff DWH] [ZEPPELIN-2216] added tests
    5a482a0 [Tinkoff DWH] [ZEPPELIN-2216] fix tests
    3977722 [Tinkoff DWH] Merge remote-tracking branch 'origin/master' into ZEPPELIN-2216
    c0436a2 [Tinkoff DWH] [ZEPPELIN-2216] general solution to precode. refactoring jdbc precode