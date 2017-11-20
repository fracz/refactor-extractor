commit 835b82faf7c82b088236909aa021a0b34202b96a
Author: 1ambda <1amb4a@gmail.com>
Date:   Mon Jan 9 07:58:51 2017 +0900

    [ZEPPELIN-1914] Preserve spaces, newlines in text output (BUG)

    ### What is this PR for?

    Fixed to preserve spaces, newlines in text output.

    ![image](https://cloud.githubusercontent.com/assets/4968473/21754393/5cbad382-d642-11e6-833d-c97018ccc8e9.png)

    ### What type of PR is it?
    [Bug Fix]

    ### Todos
    * [x] - Refactored `result.controll.js`
    * [x] - Removed the unused html file `result-results.html`
    * [x] - Preserved spaces, newlines

    ### What is the Jira issue?

    [ZEPPELIN-1914](https://issues.apache.org/jira/browse/ZEPPELIN-1914)

    ### How should this be tested?

    1. Build Zeppelin with Spark 2.0, SparkR
    2. Run Zeppelin with Spark 2.0+
    3. Make sure that your `spark` interpreter can use `spark.r`
    4. Execute this paragraph

    (**Of course, you can use other paragraph results which include consequent spaces, newlines instead of**)

    ```
    %sparkrr

    mtcarsDF <- createDataFrame(mtcars)
    model <- glm(vs ~ mpg + disp + hp + wt , data = mtcarsDF, family = "binomial")
    summary(model)
    ```

    ### Screenshots (if appropriate)

    ![image](https://cloud.githubusercontent.com/assets/4968473/21754397/6ecdaeb4-d642-11e6-9e5e-63630a45aa92.png)

    ### Questions:
    * Does the licenses files need update? - NO
    * Is there breaking changes for older versions? - NO
    * Does this needs documentation? - NO

    Author: 1ambda <1amb4a@gmail.com>

    Closes #1874 from 1ambda/ZEPPELIN-1914/respect-spaces-in-TEXT-output and squashes the following commits:

    4c2aa12 [1ambda] fix: Respect whitespaces in TEXT result
    8a3c051 [1ambda] chore: Remove unused file
    a9db952 [1ambda] refactor: Use 1 funcs to get textElem
    a9d9409 [1ambda] style: Remove useless newlines in R related files