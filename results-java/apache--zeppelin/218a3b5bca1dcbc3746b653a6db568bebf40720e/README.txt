commit 218a3b5bca1dcbc3746b653a6db568bebf40720e
Author: Mina Lee <minalee@nflabs.com>
Date:   Sat Jan 30 08:55:58 2016 -0800

    [Zeppelin-630] Introduce new way of dependency loading to intepreter

    ### What is this PR for?
    With this PR user will be able to set external libraries to be loaded to specific interpreter.

    Note that the scope of this PR is downloading libraries to local repository, not distributing them to other nodes. Only spark interpreter distributes loaded dependencies to worker nodes at the moment.

    Here is a brief explanation how the code works.
    1. get rest api request for interpreter dependency setting from front-end
    2. download the libraries in `ZEPPELIN_HOME/local-repo` and copy them to `ZEPPELIN_HOME/local-repo/{interpreterId}`
    3. `ZEPPELIN_HOME/local-repo/{interpreterId}/*.jar` are added to interpreter classpath when interpreter process starts

    ### What type of PR is it?
    Improvement

    ### Todos
    * [x] Add tests
    * [x] Update docs

    ### Is there a relevant Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-630
    And this PR will resolve [ZEPPELIN-194](https://issues.apache.org/jira/browse/ZEPPELIN-194) [ZEPPELIN-381](https://issues.apache.org/jira/browse/ZEPPELIN-381) [ZEPPELIN-609](https://issues.apache.org/jira/browse/ZEPPELIN-609)

    ### How should this be tested?
    1. Add repository(in interpreter menu, click gear button placed top right side)

        ```
    id: spark-packages
    url: http://dl.bintray.com/spark-packages/maven
    snapshot: false
        ```
    2. Set dependency in spark interpreter(click edit button of spark interpreter setting)

        ```
    artifact: com.databricks:spark-csv_2.10:1.3.0
        ```
    3. Download example csv file

        ```
    $ wget https://github.com/databricks/spark-csv/raw/master/src/test/resources/cars.csv
        ```
    4. run below code in paragraph

        ```
    val df = sqlContext.read
        .format("com.databricks.spark.csv")
        .option("header", "true") // Use first line of all files as header
        .option("inferSchema", "true") // Automatically infer data types
        .load("file:///your/download/path/cars.csv")
    df.registerTempTable("cars")
        ```
        ```
    %sql select * from cars
        ```

    ### Screenshots (if appropriate)
    * Toggle repository list
    <img width="1146" alt="screen shot 2016-01-25 at 12 24 44 pm" src="https://cloud.githubusercontent.com/assets/8503346/12563475/52f060ac-c35f-11e5-8621-d8eb97b4d6a1.png">

    * Add new repository
    <img width="1146" alt="screen shot 2016-01-25 at 12 25 23 pm" src="https://cloud.githubusercontent.com/assets/8503346/12563472/52eb545e-c35f-11e5-9050-a5306d2765f1.png">

    * Show repository info
    <img width="1146" alt="screen shot 2016-01-25 at 12 25 28 pm" src="https://cloud.githubusercontent.com/assets/8503346/12563473/52ebab84-c35f-11e5-9acb-3a356c855dc7.png">

    * Interpreter dependency
    <img width="1146" alt="screen shot 2016-01-25 at 12 27 27 pm" src="https://cloud.githubusercontent.com/assets/8503346/12563471/52eadd9e-c35f-11e5-8e1a-f583ea8800aa.png">

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions?
      - For the users who use rest api for creat/update interpreter setting, `dependencies` object should be added to request payload.
      - %dep interpreter is deprecated. The functionality is still there, but recommend to load third party dependency via interpreter menu.

    * Does this needs documentation? Yes

    Author: Mina Lee <minalee@nflabs.com>

    Closes #673 from minahlee/ZEPPELIN-630 and squashes the following commits:

    62a75c9 [Mina Lee] Merge branch 'master' of https://github.com/apache/incubator-zeppelin into ZEPPELIN-630
    545c173 [Mina Lee] Change variable name LOCAL_REPO_DIR -> LOCAL_INTERPRETER_REPO
    1e3dd47 [Mina Lee] Fix docs indentation
    320f400 [Mina Lee] Add documentation
    6b90c3d [Mina Lee] Fix mislocated interpreter setting save/cancel button
    e161b98 [Mina Lee] Add tests and split ZeppelinRestApiTest into two files
    387e21e [Mina Lee] Close input tag
    ee7532b [Mina Lee] Combine catch block for readability
    eb4a78f [Mina Lee] Handle url with file protocol for repository URL input field
    bae0c02 [Mina Lee] * Fix DependencyResolver addRepo/delRepo method * Manage repository information in `conf/interpreter.json` * Front-end modification to manage repository list * Add RestApi for adding/deleting repository * Fix tests
    fe9cb92 [Mina Lee] Enable adding interpreter dependency via GUI
    d5c931b [Mina Lee] Fix test after rebase
    1b6a818 [Mina Lee] Remove test with unused ZeppelinContext load() method
    37005c5 [Mina Lee] Remove unused methods and add deprecated message for dep interpreter
    2cd715c [Mina Lee] Add env variable/property to configuration template files
    848d931 [Mina Lee] Make external libraries to be added to interpreter process classpath