commit 30f60248cf634d3eb044512450a72f1784d17a9b
Author: tinkoff-dwh <tinkoff.dwh@gmail.com>
Date:   Sun Oct 15 14:13:28 2017 +0500

    [FIX] fix converter forms to json

    ### What is this PR for?
    Fix converter for forms

    ### What type of PR is it?
    [Bug Fix]

    ### How should this be tested?
    jdbc
    1. `select '${checkbox:name=1|2,first|second}'`

    ### Screenshots (if appropriate)
    before
    ![before](https://user-images.githubusercontent.com/25951039/31577235-44433782-b124-11e7-8479-85ec5395c3a0.png)

    after
    ![after](https://user-images.githubusercontent.com/25951039/31577234-441a8a58-b124-11e7-81fc-902ca4847fcb.png)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? no

    Author: tinkoff-dwh <tinkoff.dwh@gmail.com>

    Closes #2623 from tinkoff-dwh/forms-fix and squashes the following commits:

    fdb6cfe8 [tinkoff-dwh] [FIX] checkstyle
    08cf9213 [tinkoff-dwh] [FIX] checkstyle
    b8415507 [tinkoff-dwh] [FIX] refactoring of context converter. add test to check converter of contexts.
    17ec2dbc [tinkoff-dwh] [FIX] fix converter forms to json