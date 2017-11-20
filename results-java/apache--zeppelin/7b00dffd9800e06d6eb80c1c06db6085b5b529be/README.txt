commit 7b00dffd9800e06d6eb80c1c06db6085b5b529be
Author: AhyoungRyu <fbdkdud93@hanmail.net>
Date:   Sun Jun 12 13:40:37 2016 -0700

    [ZEPPELIN-982] Improve interpreter completion API

    ### What is this PR for?
    When people implement a new interpreter, they extend [interpreter.java](https://github.com/apache/zeppelin/blob/master/zeppelin-interpreter/src/main/java/org/apache/zeppelin/interpreter/Interpreter.java) as described in [here](https://zeppelin.apache.org/docs/0.6.0-SNAPSHOT/development/writingzeppelininterpreter.html). Among the several methods in [interpreter.java](https://github.com/apache/zeppelin/blob/master/zeppelin-interpreter/src/main/java/org/apache/zeppelin/interpreter/Interpreter.java), [completion API](https://github.com/apache/zeppelin/blob/master/zeppelin-interpreter/src/main/java/org/apache/zeppelin/interpreter/Interpreter.java#L109) enables auto-completion.

    However this API is too simple compared to other project's auto-completion and hard to add more at the moment. So for the aspect of further expansion, it would be better to separate and restructure this API before the this release( 0.6.0 ).

    ### What type of PR is it?
    Improvement

    ### Todos
    * [x] - Create new structure : `InterpreterCompletion` in `RemoteInterpreterService.thrift` and regenerate `zeppelin-interpreter/src/main/java/org/apache/zeppelin/interpreter/thrift/*` files
    * [x] - Change all existing `List<String> completion` -> `List<InterpreterCompletion> completion`
    * [x] - Change `paragraph.controller.js` to point real `name` and `value`

    ### What is the Jira issue?
    [ZEPPELIN-982](https://issues.apache.org/jira/browse/ZEPPELIN-982)

    ### How should this be tested?
    Since this improvement is just API change, it should work same as before. So after applying this patch, and check whether auto-completion works well or not.

    Use `. + ctrl` for auto-completion. For example,

    ```
    %spark
    sc.version
    ```

    When after typing `sc.` and pushing  `. + ctrl` down, `version` should be shown in the auto-completion list.

    ### Screenshots (if appropriate)
    ![auto_completion](https://cloud.githubusercontent.com/assets/10060731/15952521/72937782-2e76-11e6-8246-4faf0dd77a5b.gif)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: AhyoungRyu <fbdkdud93@hanmail.net>

    Closes #984 from AhyoungRyu/ZEPPELIN-982 and squashes the following commits:

    311dc29 [AhyoungRyu] Fix travis
    9d384ec [AhyoungRyu] Address @minalee feedback
    fdfae8f [AhyoungRyu] Address @jongyoul review
    bd4f8c0 [AhyoungRyu] Remove abstract and make it return null by default
    f8352c7 [AhyoungRyu] Fix travis error
    43d81f6 [AhyoungRyu] Remove console.log
    24912fa [AhyoungRyu] Fix type casting error in SparkInterpreter
    80e295b [AhyoungRyu] Change return type
    bd04c22 [AhyoungRyu] Apply new InterpreterCompletion class to all interpreter class files
    c283043 [AhyoungRyu] Apply new InterpreterCompletion class under zeppelin-zengine/
    dbecc51 [AhyoungRyu] Apply new InterpreterCompletion class under zeppelin-server/
    6449455 [AhyoungRyu] Apply new InterpreterCompletion class under zeppelin-interpreter/
    919b159 [AhyoungRyu] Add automatically generated thrift class
    9e69e11 [AhyoungRyu] Change v -> v.name & v.value in front
    73e374e [AhyoungRyu] Define InterpreterCompletion structure to thrift file