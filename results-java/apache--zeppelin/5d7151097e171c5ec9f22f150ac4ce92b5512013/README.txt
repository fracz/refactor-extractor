commit 5d7151097e171c5ec9f22f150ac4ce92b5512013
Author: Jeff Zhang <zjffdu@apache.org>
Date:   Mon Sep 4 21:54:56 2017 +0800

    ZEPPELIN-2898. Support Yarn-Cluster for Spark Interpreter

    ### What is this PR for?
    This is the first version for supporting yarn-cluster of `SparkInterpreter`.   I just delegate all the function to `spark-submit` as yarn-cluster is natively supported by spark, we don't need to reinvent the wheel. But there's still improvement to be done in future, e.g. I put some spark specific logic in `InterpreterSetting` which is not a good practise.  I plan to improve it when I refactor the `Interpreter` class (ZEPPELIN-2685).

    Besides that, I also add `MiniHadoopCluster` & `MiniZeppelin` which help for the integration test of yarn-client & yarn-cluster mode, otherwise I have to manually verify yarn-client & yarn-cluster mode which would easily cause regression issue in future.

    To be noticed:

    * SPARK_HOME must be specified for yarn-cluster mode
    * HADOOP_CONF_DIR must be specified for yarn-cluster mode

    ### What type of PR is it?
    [Feature]

    ### Todos
    * [ ] - Task

    ### What is the Jira issue?
    https://github.com/zjffdu/zeppelin/tree/ZEPPELIN-2898

    ### How should this be tested?
    System test is added in `SparkInterpreterIT`.

    ### Questions:
    * Does the licenses files need update?  No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jeff Zhang <zjffdu@apache.org>

    Closes #2577 from zjffdu/ZEPPELIN-2898 and squashes the following commits:

    9da7c4b [Jeff Zhang] ZEPPELIN-2898. Support Yarn-Cluster for Spark Interpreter