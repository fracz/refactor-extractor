commit 9b72c894017880bd4a2a0c018c6f9d9b42a5d51d
Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>
Date:   Wed Feb 17 11:23:52 2016 +0530

    selenium test spark, pyspark and sparkSql

    ### What is this PR for?
    Test functionality of spark, pyspark, sparksql

    ### What type of PR is it?
    Improvement

    ### Todos
    * [x] - Selenium for spark
    * [x] - Selenium for pyspark
    * [x] - Selenium for sparksql
    * [x] - refactor with https://github.com/apache/incubator-zeppelin/pull/619

    ### Is there a relevant Jira issue?
    ZEPPELIN-587

    ### How should this be tested?
    On macOS

        PATH=~/Applications/Firefox.app/Contents/MacOS/:$PATH CI="" \
        mvn -Dtest=org.apache.zeppelin.integration.TestSparkParagraph -Denforcer.skip=true \
        test -pl zeppelin-server

    Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>

    Closes #654 from prabhjyotsingh/ZEPPELIN-587 and squashes the following commits:

    8f24695 [Prabhjyot Singh] use handleException in all other test cases remove test for spark 1.1.1 more meaningful log message
    28dfc55 [Prabhjyot Singh] thorwong exception similar to https://github.com/apache/incubator-zeppelin/pull/709
    21bcc45 [Prabhjyot Singh] Merge remote-tracking branch 'origin/master' into ZEPPELIN-587
    b05b81b [Prabhjyot Singh] have SHIFT_ENTER enum
    9a206f4 [Prabhjyot Singh] add missing endToEndTestEnabled check for testSparkInterpreterDependencyLoading
    ab03287 [Prabhjyot Singh] have static import of AbstractZeppelinIT.HelperKeys.*, and rg.openqa.selenium.Keys.*
    5187a16 [Prabhjyot Singh] check if spark version is less than 1.3 then don't append ".toDF()"
    6927f7e [Prabhjyot Singh] CI FIX
    9236e3c [Prabhjyot Singh] implemeting @bzz review comments
    2c28758 [Prabhjyot Singh] missed refactor with #619
    d49344f [Prabhjyot Singh] selenium test spark, pyspark and sparkSql