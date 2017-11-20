commit bed82eb4bdf20db5c894444ccaed8a8631846997
Author: 1ambda <1amb4a@gmail.com>
Date:   Fri May 19 00:43:23 2017 +0900

    [ZEPPELIN-2411] Improve Table

    ### What is this PR for?

    **Improve Table**

    - column filter
    - persist column state: type, order, hide/show, sorting, pinning
    - pagination
    - `setting` menu to configure table UI
    - group by + aggregation

    And **all these things are persisted and synchronized among web socket clients**

    See the screenshot section for more detail.

    ### What type of PR is it?
    [Improvement]

    ### Todos
    * [x] - Remove handsontable dependencies
    * [x] - Use npm packaged moment* packages.
    * [x] - Apply ui-grid
    * [x] - Add setting menu
    * [x] - Fix some issues
    * [x] - Persist column type

    ### What is the Jira issue?

    [ZEPPELIN-2411](https://issues.apache.org/jira/browse/ZEPPELIN-2411)

    ### How should this be tested?

    1. Build: `mvn clean package -DskipTests; ./bin/zeppelin-daemon.sh restart`
    2. Open a note and create tables. If you don't have proper paragraphs, use this snippet.

    ```scala
    %spark

    import org.apache.commons.io.IOUtils
    import java.net.URL
    import java.nio.charset.Charset

    val bankText = sc.parallelize(
        IOUtils.toString(
            new URL("https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv"),
            Charset.forName("utf8")).split("\n"))

    case class Bank(
        age: Integer,
        job: String,
        marital: String,
        education: String,
        balance: Integer,
        housing: Boolean,
        loan: Boolean,
        contact: String,
        day: Int,
        month: String,
        duration: Int,
        y: Boolean
    )

    val bank = bankText.map(s => s.split(";")).filter(s => s(0) != "\"age\"").map(
        s => Bank(s(0).toInt,
                s(1).replaceAll("\"", ""),
                s(2).replaceAll("\"", ""),
                s(3).replaceAll("\"", ""),
                s(5).replaceAll("\"", "").toInt,
                if (s(6).replaceAll("\"", "") == "yes") true else false,
                if (s(7).replaceAll("\"", "") == "yes") true else false,
                s(8).replaceAll("\"", ""),
                s(9).replaceAll("\"", "").toInt,
                s(10).replaceAll("\"", ""),
                s(11).replaceAll("\"", "").toInt,
                if (s(16).replaceAll("\"", "") == "yes") true else false
            )
    ).toDF()
    bank.registerTempTable("bank")
    ```

    ```sql
    select age, education, job, balance from bank limit 1000
    ```

    ### Screenshots (if appropriate)

    #### Before

    ![image](https://cloud.githubusercontent.com/assets/4968473/25803644/d0d81524-3432-11e7-8cf6-dde16465a447.png)

    #### After: Filter

    ![2411_new_filter](https://cloud.githubusercontent.com/assets/4968473/25880089/4e1e6a5e-3570-11e7-8339-36e712a170cc.gif)

    #### After: Column related features

    ![2411_new_column](https://cloud.githubusercontent.com/assets/4968473/25880690/d8a4da02-3573-11e7-9851-cc359fd6075c.gif)

    #### After: Pagination

    ![2411_new_pagination](https://cloud.githubusercontent.com/assets/4968473/25880646/ad3b5ea4-3573-11e7-8e03-e56af683eb9b.gif)

    #### After: Group, Aggregation

    ![2411_new_group_aggr](https://cloud.githubusercontent.com/assets/4968473/25880819/97ba7a82-3574-11e7-8dc4-8d39df6a65a0.gif)

    #### After: Synchronized

    ![2411_new_sync](https://cloud.githubusercontent.com/assets/4968473/25880669/c1784bb6-3573-11e7-966d-cb21d222d683.gif)

    ### Questions:
    * Does the licenses files need update? - YES, updated
    * Is there breaking changes for older versions? - NO
    * Does this needs documentation? - NO

    Author: 1ambda <1amb4a@gmail.com>

    Closes #2323 from 1ambda/ZEPPELIN-2411/prettify-table and squashes the following commits:

    c56edca [1ambda] fix: Change table cell color to white
    2047c5f [1ambda] feat: Render HTML display in cell
    2a79915 [1ambda] fix: Add tooltip and change icon for restore
    39d64c1 [1ambda] fixup: tooltip
    2b40978 [1ambda] fix: Remove save btn in table setting panel
    bd2b4c8 [1ambda] fix: Add tooltip for description
    3a87718 [1ambda] fix: DON'T display type of table options
    1885172 [1ambda] fix: Remove pagination related options from table spec
    4878614 [1ambda] fix: order for table options
    e4d2c37 [1ambda] fix: Disable selection
    06e7841 [1ambda] feat: Add option for selection
    a9d313d [1ambda] fix: Remove desc in opt specs
    cf597bf [1ambda] fix: Reset tableColumnTypeState
    399116b [1ambda] fix: SparkParagraphIT for table
    84c04ac [1ambda] fix: Remove duplicated console
    fdcfe7f [1ambda] chore: Remove unused license
    fc2f6d0 [1ambda] fix: css loader for karma test
    4742a28 [1ambda] fix: RAT issue
    8ea8ae5 [1ambda] fix: DON'T debounce for emit
    a1ae980 [1ambda] feat: Persist type
    d928290 [1ambda] fix: Use isRestoring flag to avoid triggering event when initializing
    a262b79 [1ambda] fix: Persist tableOption immediately
    f83c070 [1ambda] fix: Commit graph config when closing
    6db38ae [1ambda] feat: Add missing change events
    911c0e7 [1ambda] fix: Prevent recursive emitting
    d47ccc8 [1ambda] feat: Persist grid state
    b433d7f [1ambda] refactor: Add getGrid* funcs
    1480841 [1ambda] fix: Remove refresh in menu actions
    35d99e9 [1ambda] fix: enable scroll in col menus
    25a72fe [1ambda] feat: Add types to menu
    acc38cf [1ambda] feat: Add paginiation table opts
    8793d8f [1ambda] fix: set valid pagination opts
    744dc66 [1ambda] docs: Update desc for table option
    8bd8256 [1ambda] fix: persist initial config
    78cec42 [1ambda] feat: resetTableOption
    475bc31 [1ambda] feat: Add table option
    fc0abd4 [1ambda] feat: Use tabledata
    85cdd8e [1ambda] feat: render setting for table
    5ee6a2e [1ambda] fix: Remove handsonhelper while using moment form npm
    2444855 [1ambda] refactor: variable name
    ed17862 [1ambda] fix: Remove unused css
    1f61260 [1ambda] refactor: extract table related css to display-table.css