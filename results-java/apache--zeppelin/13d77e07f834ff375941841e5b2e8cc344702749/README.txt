commit 13d77e07f834ff375941841e5b2e8cc344702749
Author: Peilin Yang <yangpeilyn@gmail.com>
Date:   Wed Nov 30 08:45:03 2016 -0500

    [ZEPPELIN-1371]add text/numeric conversion support to table display

    ### What is this PR for?

    We people in Twitter have great demand of adding a flexible drop down menu for the columns in the tables which enables the text/numeric conversion.
    This is because people want some columns to be of string type which, for example, fits to the underlying DB definition.
    The use cases of the change include:
    1. For now sorting on the columns is always lexicographically because Zeppelin front end treats the data as strings. It the values in the table can be convertible between string and number then we can sort the column by numeric values. I have filed another ticket for this: https://issues.apache.org/jira/browse/ZEPPELIN-1372
    2. Since the users know more about their data than us it would be nice if we let the users decide what is the real type of their data. It is annoying if user wants the column to be strings but the front end forcefully inserts commas in it. In some scenarios, users may also want to copy/paste the table to somewhere else. If people want to remove the commas before other actions then that will be a nightmare....
    ### What type of PR is it?

    [Improvement | Feature]
    ### Todos
    - [ ] - Task
    ### What is the Jira issue?
    - https://issues.apache.org/jira/browse/ZEPPELIN-1371
    ### How should this be tested?
    1. Click on the dropdown menu would convert the text/number of that column.
    2. Other functionalities esp. the sorting function should not be affected.
    ### Screenshots (if appropriate)

    ![image](https://cloud.githubusercontent.com/assets/3334391/18445617/76071f8e-78ec-11e6-93a6-fdcf7b85b6e9.png)
    ![image](https://cloud.githubusercontent.com/assets/3334391/18446601/16cff4f0-78f1-11e6-9fd6-4010b5d77f17.png)
    ### Questions:
    - Does the licenses files need update? No
    - Is there breaking changes for older versions? Probably a noticeable change in UI
    - Does this needs documentation? No

    Author: Peilin Yang <yangpeilyn@gmail.com>

    Closes #1500 from Peilin-Yang/ypeilin/table_num_cell_format and squashes the following commits:

    e3425ee [Peilin Yang] make clean code
    ba60cb0 [Peilin Yang] update test case
    cdc06b6 [Peilin Yang] refactor based on the most recent master
    06b4ae7 [Peilin Yang] merge
    4e2a403 [Peilin Yang] bug fix
    61824d3 [Peilin Yang] update test case
    df2effb [Peilin Yang] update the test case
    39c5408 [Peilin Yang] update the test case in SparkParagraphIT
    030a1e6 [Peilin Yang] add dropdown menu applied on the latest branch