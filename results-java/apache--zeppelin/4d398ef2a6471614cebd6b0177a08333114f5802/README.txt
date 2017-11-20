commit 4d398ef2a6471614cebd6b0177a08333114f5802
Author: Tinkoff DWH <tinkoff.dwh@gmail.com>
Date:   Mon Apr 3 20:53:02 2017 +0500

    [ZEPPELIN-2297] improvements  to jdbc autocompleter

    ### What is this PR for?
    PR contains some improvements for completion (JDBC Interpreter):
    - types of completion
    - display of long values
    - refactoring of search of completions
    - uniqness of completions with type `keyword`
    - updating data in completer by pressing `Ctrl + .`
    - setting the schema filter to generate completions
    - fix highlighting code when used not default data source

    ### What type of PR is it?
    Improvement

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-2297

    ### How should this be tested?
    try to work with new completer

    ### Screenshots (if appropriate)
    **1. Types of completion**
    ![1](https://cloud.githubusercontent.com/assets/25951039/24449367/758eeeac-1490-11e7-863f-bf1b313a3f4d.png)

    **2. Display of long values**
    before
    ![2297_before_long_caption](https://cloud.githubusercontent.com/assets/25951039/24449397/8ecd3072-1490-11e7-8fd4-415424ef337e.gif)
    after
    ![2297_after_long_caption](https://cloud.githubusercontent.com/assets/25951039/24449413/9c7a36b6-1490-11e7-9d7c-cbbdac71cbe7.gif)

    **3. Refactoring of search of completions. Updating data in completer by pressing `Ctrl + .`**
    before
    ![2297_before_refactoring_search](https://cloud.githubusercontent.com/assets/25951039/24449463/c1801214-1490-11e7-84a8-25c887b68d65.gif)
    after
    ![2297_after_refactoring_search](https://cloud.githubusercontent.com/assets/25951039/24449567/1079bdc0-1491-11e7-8409-5187aeceb428.gif)

    **4. uniqness of completions with type keyword**
    before
    ![2297_before_uniq](https://cloud.githubusercontent.com/assets/25951039/24449615/4e20c8d0-1491-11e7-94cc-c86aab886c53.gif)
    after
    ![2297_after_uniq](https://cloud.githubusercontent.com/assets/25951039/24449635/5cf59aca-1491-11e7-8ee1-31ea3cdacb3e.gif)

    **5. fix highlighting code when used not default data source**
    before
    ![2297_before_inrpret_name](https://cloud.githubusercontent.com/assets/25951039/24449730/b6c8d62a-1491-11e7-8dc3-39fa6975c8c3.gif)
    after
    ![2297_after_inrpret_name](https://cloud.githubusercontent.com/assets/25951039/24449738/baf63e18-1491-11e7-8711-12557a674212.gif)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: Tinkoff DWH <tinkoff.dwh@gmail.com>

    Closes #2203 from tinkoff-dwh/ZEPPELIN-2297 and squashes the following commits:

    b86b57a [Tinkoff DWH] [ZEPPELIN-2297] small fix to compute caption
    8552049 [Tinkoff DWH] [ZEPPELIN-2297] schema filters
    5308f1e [Tinkoff DWH] [ZEPPELIN-2297] updating completions
    ef6c9cb [Tinkoff DWH] Merge remote-tracking branch 'origin/ZEPPELIN-2297' into ZEPPELIN-2297
    1e05a68 [Tinkoff DWH] [ZEPPELIN-2297] fix uniqueness keywords
    ec3cd3b [Tinkoff DWH] [ZEPPELIN-2297] fix uniqueness keywords
    2b58cc5 [Tinkoff DWH] [ZEPPELIN-2297] refactoring search completions
    7b5835d [Tinkoff DWH] [ZEPPELIN-2297] compute caption of copletion
    1c74384 [Tinkoff DWH] [ZEPPELIN-2297] add type of completion