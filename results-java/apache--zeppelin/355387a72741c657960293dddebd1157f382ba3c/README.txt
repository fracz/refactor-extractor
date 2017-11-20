commit 355387a72741c657960293dddebd1157f382ba3c
Author: purechoc <purechoc@ncsoft.com>
Date:   Fri Dec 23 19:35:55 2016 +0900

    [ZEPPELIN-1758] support livy pyspark Interpreter's magic function

    ### What is this PR for?
    support livy pyspark Interpreter's magic function

    ### What type of PR is it?
     Improvement

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1758

    ### How should this be tested?
    test code

    ```
    %livy.pyspark

    t = [{"name":"userA", "role":"roleA"}, {"name":"userB", "role":"roleB"}, {"name":"userC"}]
    %table t
    ```

    ```
    %livy.sparkr
    plot(iris, col = heat.colors(3))
    ```

    ### Screenshots (if appropriate)
    ![magic](https://cloud.githubusercontent.com/assets/16571121/21447042/c30933e2-c911-11e6-950b-b71864ecf0e7.png)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: purechoc <purechoc@ncsoft.com>
    Author: chrischo <purechoc.en@gmail.com>

    Closes #1729 from purechoc/support-livy-magic and squashes the following commits:

    c4a0952 [chrischo] fixed valiable name
    2cbc782 [chrischo] fixed valiable name
    e3dcf42 [purechoc] fixed wrong code
    5f26592 [purechoc] fixed wrong code
    641f482 [purechoc] change to using StringUtils
    0699e84 [purechoc] change to using StringBuilder
    1cdd7ff [purechoc] add test in LivyInterpreterIT
    56f1b78 [purechoc] refactoring
    2af35b9 [purechoc] Merge remote-tracking branch 'upstream2/master' into support-livy-magic
    c33ac76 [purechoc] fixed some wrong code
    5fe38d2 [purechoc] support table magic