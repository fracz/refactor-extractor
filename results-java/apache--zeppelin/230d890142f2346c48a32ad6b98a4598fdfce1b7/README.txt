commit 230d890142f2346c48a32ad6b98a4598fdfce1b7
Author: Alexander Bezzubov <bzz@apache.org>
Date:   Thu Jun 23 10:25:49 2016 +0900

    ZEPPELIN-1048: Pandas support for python interpreter

    ### What is this PR for?
    Display Pandas DataFrame using Zeppelin's Table Display system.

    ### What type of PR is it?
    Feature

    ### Todos
    * [x] fix NPE in logs on empty paragraph execution
    * [x] matplotlib: refactor `zeppelin_show(plt)` -> `z.show(plt)`
    * [x] pandas: support `z.show(df)`
    * [x] update docs

    ### What is the Jira issue?
    [ZEPPELIN-1048](https://issues.apache.org/jira/browse/ZEPPELIN-1048)

    ### How should this be tested?
    "Zeppelin Tutorial: Python - matplotlib basic" should work, and

    ```python
    import pandas as pd
    rates = pd.read_csv("bank.csv", sep=";")
    z.show(rates)
    ```
    ### Screenshots (if appropriate)
    ![screen shot 2016-06-23 at 10 29 00](https://cloud.githubusercontent.com/assets/5582506/16289133/85f0ddbc-392d-11e6-86a3-28d10e73f68d.png)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? Yes

    Author: Alexander Bezzubov <bzz@apache.org>

    Closes #1067 from bzz/python/pandas-support and squashes the following commits:

    3b1ad36 [Alexander Bezzubov] Python: update docs to reffer new API
    ee6668b [Alexander Bezzubov] Python: update docs, add Pandas integration
    71be418 [Alexander Bezzubov] Python: limit 1000 for table display system on DataFrame
    52e787d [Alexander Bezzubov] Python: pandas DataFrame using Table display system
    bc91b86 [Alexander Bezzubov] Python: skip interpreting empty paragraphs
    a7248cd [Alexander Bezzubov] Python: draft of pandas support
    15646a1 [Alexander Bezzubov] Python: refactoring to z.show()