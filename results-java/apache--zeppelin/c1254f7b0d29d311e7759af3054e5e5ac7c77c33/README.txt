commit c1254f7b0d29d311e7759af3054e5e5ac7c77c33
Author: Jun <i2r.jun@gmail.com>
Date:   Thu Nov 10 02:18:02 2016 +0900

    [ZEPPELIN-1628] Enable renaming note from the main page

    ### What is this PR for?
    Now users can rename a note from the main page! This new feature will improve UX.

    I divided [ZEPPELIN-1598](https://issues.apache.org/jira/browse/ZEPPELIN-1598) into sub-tasks since renaming folder gonna be huge. I will open PR for [ZEPPELIN-1629](https://issues.apache.org/jira/browse/ZEPPELIN-1629) after merging this PR.

    By the way, I have a question! Does a `writer` can rename a note? Currently, only an owner can rename a note.

    ### What type of PR is it?
    [Feature]

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1628

    ### Screenshots (if appropriate)
    ![rename_note](https://cloud.githubusercontent.com/assets/8201019/20057051/a16b221c-a52c-11e6-900e-f88031ff1246.gif)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Jun <i2r.jun@gmail.com>

    Closes #1609 from tae-jun/ZEPPELIN-1628 and squashes the following commits:

    7d3f7bb [Jun] Fix factory/noteList.js test errors.
    54bae60 [Jun] Add rename modal input validation
    3eadcd8 [Jun] Add folder id
    fb8b35f [Jun] Correct indent to pass style check
    c019b9e [Jun] Rename a note from the main page