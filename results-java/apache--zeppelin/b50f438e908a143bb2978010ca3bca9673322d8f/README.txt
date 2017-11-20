commit b50f438e908a143bb2978010ca3bca9673322d8f
Author: Ryu Ah young <fbdkdud93@hanmail.net>
Date:   Tue Feb 2 18:25:20 2016 +0900

    ZEPPELIN-646: Shell interpreter output streaming

    ### What is this PR for?
    After #611 merged, Zeppelin provides streaming output for **spark** and **pyspark** interpreter. For the further improvement, I changed a few code lines using <code>[InterpreterContext](https://github.com/apache/incubator-zeppelin/blob/master/zeppelin-interpreter/src/main/java/org/apache/zeppelin/interpreter/InterpreterContext.java#L66)</code> so that **sh** interpreter can be available too.

    ### What type of PR is it?
    Improvement

    ### Todos

    ### Is there a relevant Jira issue?
    [ZEPPELIN-646: Shell interpreter output streaming](https://issues.apache.org/jira/browse/ZEPPELIN-646)
    [ZEPPELIN-554: Streaming interpreter output to front-end]()

    ### How should this be tested?
    After applying this PR, run this below code with `sh` interpreter in Zeppelin.
    ```
    date && sleep 3 &&  date
    ```

    Then you can see two timestamps which have 3 seconds gap.

    ### Screenshots (if appropriate)
    ![shell_interpreter](https://cloud.githubusercontent.com/assets/10060731/12745026/b12e7b28-c9da-11e5-8832-0ebc74bbf4f3.gif)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Ryu Ah young <fbdkdud93@hanmail.net>

    Closes #683 from AhyoungRyu/ZEPPELIN-646 and squashes the following commits:

    a9d2e2b [Ryu Ah young] ZEPPELIN-646: Shell interpreter output streaming