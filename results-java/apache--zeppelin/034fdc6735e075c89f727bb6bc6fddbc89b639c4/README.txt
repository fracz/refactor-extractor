commit 034fdc6735e075c89f727bb6bc6fddbc89b639c4
Author: 1ambda <1amb4a@gmail.com>
Date:   Wed Jan 11 07:56:35 2017 +0900

    [ZEPPELIN-1917] Improve python.conda interpreter

    ### What is this PR for?

    Add missing commands to the `python.conda` interpreter

    - `conda info`
    - `conda list`
    - `conda create`
    - `conda install`
    - `conda uninstall (alias of remove)`
    - `conda env *`

    #### Implementation Detail

    The reason I modified `PythonProcess` is due to NPE

    ```java
    // https://github.com/apache/zeppelin/blob/master/python/src/main/java/org/apache/zeppelin/python/PythonProcess.java#L107-L118

      public String sendAndGetResult(String cmd) throws IOException {
        writer.println(cmd);
        writer.println();
        writer.println("\"" + STATEMENT_END + "\"");
        StringBuilder output = new StringBuilder();
        String line = null;

        // NPE when line is null
        while (!(line = reader.readLine()).contains(STATEMENT_END)) {
          logger.debug("Read line from python shell : " + line);
          output.append(line + "\n");
        }
        return output.toString();
      }
    ```

    ```
    java.lang.NullPointerException
    at org.apache.zeppelin.python.PythonProcess.sendAndGetResult(PythonProcess.java:113)
    at org.apache.zeppelin.python.PythonInterpreter.sendCommandToPython(PythonInterpreter.java:250)
    at org.apache.zeppelin.python.PythonInterpreter.bootStrapInterpreter(PythonInterpreter.java:272)
    at org.apache.zeppelin.python.PythonInterpreter.open(PythonInterpreter.java:100)
    at org.apache.zeppelin.python.PythonCondaInterpreter.restartPythonProcess(PythonCondaInterpreter.java:139)
    at org.apache.zeppelin.python.PythonCondaInterpreter.interpret(PythonCondaInterpreter.java:88)
    at org.apache.zeppelin.interpreter.LazyOpenInterpreter.interpret(LazyOpenInterpreter.java:94)
    at org.apache.zeppelin.interpreter.remote.RemoteInterpreterServer$InterpretJob.jobRun(RemoteInterpreterServer.java:494)
    at org.apache.zeppelin.scheduler.Job.run(Job.java:175)
    at org.apache.zeppelin.scheduler.FIFOScheduler$1.run(FIFOScheduler.java:139)
    at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:471)
    at java.util.concurrent.FutureTask.run(FutureTask.java:262)
    at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$201(ScheduledThreadPoolExecutor.java:178)
    at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:292)
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
    at java.lang.Thread.run(Thread.java:745)
    ```

    ### What type of PR is it?
    [Improvement | Refactoring]

    ### Todos
    * [x] - info
    * [x] - list
    * [x] - create
    * [x] - install
    * [x] - uninstall (= remove)
    * [x] - env *

    ### What is the Jira issue?

    [ZEPPELIN-1917](https://issues.apache.org/jira/browse/ZEPPELIN-1917)

    ### How should this be tested?

    1. Install [miniconda](http://conda.pydata.org/miniconda.html)
    2. Make sure that your python interpreter can use `conda` (check the Interpreter Binding page)
    3. Remove `test` conda env since we will create in the following section

    ```sh
    $ conda env remove --yes --name test
    ```

    4. Run these commands with `%python.conda`

    ```
    %python.conda info
    %python.conda env list
    %python.conda create --name test

    # you should be able to see `test` in the list
    %python.conda env list
    %python.conda activate pymysql
    %python.conda install pymysql

    # you should be able to import
    %python
    import pymysql.cursors

    %python.conda uninstall pymysql
    %python.conda deactivate pymysql

    # you should be able to see `No module named pymysql.cursor` since we deactivated
    %python
    import pymysql.cursors
    ```

    ### Screenshots (if appropriate)

    ![conda-screenshot](https://cloud.githubusercontent.com/assets/4968473/21747565/98c0e366-d5ad-11e6-8000-e293996089fa.gif)

    ### Questions:
    * Does the licenses files need update? - NO
    * Is there breaking changes for older versions? - NO
    * Does this needs documentation? - NO

    Author: 1ambda <1amb4a@gmail.com>

    Closes #1868 from 1ambda/ZEPPELIN-1917/improve-conda-interpreter and squashes the following commits:

    3ba171a [1ambda] fix: Wrap output style
    292ed6d [1ambda] refactor: Throw exception in runCommand
    2d4aa7d [1ambda] test: Add some tests
    49a4a11 [1ambda] feat: Supports other env commands
    6eb7e92 [1ambda] fix: NPE in PythonProcess
    9c5dd86 [1ambda] refactor: Activate, Deactivate
    f955889 [1ambda] fix: minor
    935cb89 [1ambda] refactor: Abstract commands
    b1c4c9f [1ambda] feat: Add conda remove (uninstall)
    e539c42 [1ambda] feat: Add conda install
    4f58fa2 [1ambda] feat: Add conda create
    7da132d [1ambda] docs: Add missing conda list description
    929ca8a [1ambda] feat: Make conda output beautiful
    0c6ebb4 [1ambda] feat: Add list conda command
    017c76f [1ambda] refactor: Import InterpreterResult.{Code, Type} to short codes
    b8a5154 [1ambda] refactor: Simplify exception flow so private funcs don't need care exceptions
    64d4bef [1ambda] style: Rename some funcs
    afc456d [1ambda] refactor: Add private to member vars
    f36fc74 [1ambda] feat: Add info command
    2eb9bf5 [1ambda] style: Remove useless newlines
    bd2564e [1ambda] refactor: PythonCondaInterpreter.interpret
    f0d69bc [1ambda] fix: Use specific command for env list in conda