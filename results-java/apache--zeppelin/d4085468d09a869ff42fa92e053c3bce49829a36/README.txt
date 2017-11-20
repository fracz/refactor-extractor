commit d4085468d09a869ff42fa92e053c3bce49829a36
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Fri Mar 31 10:36:39 2017 +0800

    ZEPPELIN-2189. The order of dynamic forms should be the order that you create them

    ### What is this PR for?
    The order of dynamic forms should be the order that you create them. So I made the following 2 changes for this:
    * change the type of forms in GUI from TreeMap to LinkedHashMap
    *  remove orderBy in paragraph-parameterizedQueryForm.html

    Besides, I also did some code refactoring.

    ### What type of PR is it?
    [Bug Fix | Improvement | Refactoring]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-2189

    ### How should this be tested?
    Tested manually.

    ### Screenshots (if appropriate)
    Before
    ![2017-02-27_1310](https://cloud.githubusercontent.com/assets/164491/24486826/9ac3c1e4-153e-11e7-8280-8cf4f6ef7560.png)

    After
    ![2017-03-30_1109](https://cloud.githubusercontent.com/assets/164491/24486828/9b733ee4-153e-11e7-9a13-44ed71aa29d8.png)

    ### Questions:
    * Does the licenses files need update?
    * Is there breaking changes for older versions?
    * Does this needs documentation?

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #2204 from zjffdu/ZEPPELIN-2189 and squashes the following commits:

    a69ab5a [Jeff Zhang] add test
    167d162 [Jeff Zhang] fix code style
    369900e [Jeff Zhang] ZEPPELIN-2189. The order of dynamic forms should be the order that you create them