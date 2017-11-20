commit 2a2a2e82b9de92d70897ce8e81ae1498949598fd
Author: Jongyoul Lee <jongyoul@gmail.com>
Date:   Thu Jun 30 23:46:56 2016 +0900

    [ZEPPELIN-1075] Merge NoteInterpreterLoader into InterpreterFactory

    ### What is this PR for?
    Removing redundant codes between `NoteInterpreterLoader` and `InterpreterFactory`, reducing the cost to add new features, and making refactoring on `InterpreterFactory` easy

    ### What type of PR is it?
    [Refactoring]

    ### Todos
    * [x] - Remove `NoteInterpreterLoader` and move the functionality into `InterpreterFactory`

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-1075

    ### How should this be tested?
    Must pass all tests

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jongyoul Lee <jongyoul@gmail.com>

    Closes #1102 from jongyoul/ZEPPELIN-1075 and squashes the following commits:

    d9816f1 [Jongyoul Lee] Fixed related codes
    a2d8104 [Jongyoul Lee] Fixed some style and removed unused codes
    28bf520 [Jongyoul Lee] Fixed style
    600de98 [Jongyoul Lee] Removed NoteInterpreterLoader Fixed some tests
    536059b [Jongyoul Lee] Duplicated all method in NoteInterpreterLoader to InterpreterFactory