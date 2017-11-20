commit 125a42ca1f8194c4b3872230e58cbdb6ff35597d
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Tue Dec 20 12:29:52 2016 +0800

    ZEPPELIN-1786. Refactor LivyHelper

    ### What is this PR for?

    This PR continue the work of livy refactoring. Here's the main changes in this PR
    * Move the code from `LivyHelper` to `BaseLivyInterprereter`
    * Define POJO for livy request and response instead of using Map (sometimes nested Map)
    * Move the livy session initialization from `interpret` to `open`
    * Add one more complicated test which use the spark basic tutorial note.
    * Support livy.sql for spark2
    * Use zeppelin.livy.create.session.timeout instead of retry count as retry count is internal implementation and user don't know what does it mean.
    * Improve travis (wrap livy related work in `setupLivy.sh`)

    ### What type of PR is it?
    [Improvement | Refactoring]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-1786

    ### How should this be tested?

    One more integration test is added

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #1751 from zjffdu/ZEPPELIN-1786 and squashes the following commits:

    30443f8 [Jeff Zhang] Fix string escape issue for livy.sql
    d6fb35d [Jeff Zhang] address comments
    6e5cbb8 [Jeff Zhang] ZEPPELIN-1786. Refactor LivyHelper