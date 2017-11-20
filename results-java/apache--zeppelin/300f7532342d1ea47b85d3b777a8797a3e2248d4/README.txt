commit 300f7532342d1ea47b85d3b777a8797a3e2248d4
Author: Lee moon soo <moon@apache.org>
Date:   Thu Jan 12 10:58:06 2017 -0800

    [ZEPPELIN-1619] Load js package as a plugin visualization

    ### What is this PR for?
    Current helium plugin application api (experimental) requires create library in java class, and need to create both backend / frontend code in the package. Which is good if your plugin requires both frontend and backend code running.

    However, when user just want to make new visualization which completely runs on front-end side in javascript, creating helium application in java project and taking care of backend code can be bit of overhead and barrier for javascript developers.

    This PR adds capability to load pure javascript package as a visualization.

    ### how it works

    1. create (copy, download) 'helium package json' file into `ZEPPELIN_HOME/helium` directory.
      The json file point visualization js package in npm repository or local file system in `artifact` field.
      `type` field in the json file need to be `VISUALIZATION`

    Here's an example (zeppelin-examples/zeppelin-example-horizontalbar/zeppelin-example-horizontalbar.json)
    ```
    {
      "type" : "VISUALIZATION",
      "name" : "zeppelin_horizontalbar",
      "description" : "Horizontal Bar chart (example)",
      "artifact" : "./zeppelin-examples/zeppelin-example-horizontalbar",
      "icon" : "<i class='fa fa-bar-chart rotate90flipX'></i>"
    }
    ```

    2. Go to helium GUI menu. (e.g. http://localhost:8080/#/helium).
      The menu will list all available packages.
    <img width="796" alt="writing_visualization_helium_menu" src="https://cloud.githubusercontent.com/assets/1540981/21749660/0f401c10-d558-11e6-9961-b6d0a9c023d8.png">

    3. click 'enable' in any package want to use.
    Once a visualization package is enabled, `HeliumVisualizationFactory` will collect all enabled visualizations and create js bundle on the fly.

    4. js bundle will be loaded on notebook and additional visualization becomes available
    ![image](https://cloud.githubusercontent.com/assets/1540981/21749729/709b2b3e-d559-11e6-8318-7f2871e7c39a.png)

    ### Programming API to create new plugin visualization.

    Simply extends [visualization.js](https://github.com/apache/zeppelin/blob/master/zeppelin-web/src/app/visualization/visualization.js) and overrides some methods, such as

    ```
      /**
       * get transformation
       */
      getTransformation() {
        // override this
      };

      /**
       * Method will be invoked when data or configuration changed
       */
      render(tableData) {
        // override this
      };

      /**
       * Refresh visualization.
       */
      refresh() {
        // override this
      };

      /**
       * method will be invoked when visualization need to be destroyed.
       * Don't need to destroy this.targetEl.
       */
      destroy() {
        // override this
      };

      /**
       * return {
       *   template : angular template string or url (url should end with .html),
       *   scope : an object to bind to template scope
       * }
       */
      getSetting() {
        // override this
      };
    ```

    This is exactly the same api that built-in visualization uses.

    an example implementation included `zeppelin-examples/zeppelin-example-horizontalbar/horizontalbar.js`.
    Actually [all built-in visualizations](https://github.com/apache/zeppelin/tree/master/zeppelin-web/src/app/visualization/builtins) are example

    ### Packaging and publishing visualization

    Each visualization will need `package.json` file (e.g. `zeppelin-examples/zeppelin-example-horizontalbar/package.json`) to be packaged.
    Package can be published in npm repository or package can be deployed to the local filesystem.

    `zeppelin-examples/zeppelin-example-horizontalbar/` is an example package that is deployed in the local filesystem

    ### Development mode

    First, locally install and enable your development package by setting `artifact` field to the development directory.
    Then run zeppelin-web in visualization development mode with following command
    ```
    cd zeppelin-web
    npm run visdev
    ```
    When you have change in your local development package, just reload your notebook. Then Zeppelin will automatically rebuild / reload the package.

    Any feedback would be appreciated!

    ### What type of PR is it?
    Feature

    ### Todos
    * [x] - Load plugin visualization js package on runtime
    * [x] - Make the feature works in zeppelin Binary package
    * [x] - Show loading indicator while 'enable' / 'disable' package
    * [x] - Add document
    * [x] - Add license of new dependency
    * [x] - Development mode
    * [x] - Propagate error to front-end
    * [x] - Display multiple versions of a package.

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1619

    ### How should this be tested?
    Build Zeppelin with `-Pexamples` flag. That'll install example visualization package `horizontalbar`.
    You'll able to select `horizontalbar` along with other built-in visualizations
    ![image](https://cloud.githubusercontent.com/assets/1540981/21655057/27d61740-d26d-11e6-88f2-02c653e102c6.png)

    To test npm online package install capability,  Place [zeppelin-bubble.json](https://github.com/Leemoonsoo/zeppelin-bubble/blob/master/zeppelin-bubble.json) in hour local registry (`ZEPPELIN_HOME/helium`) and enable it in Helium gui menu.
    And then zeppelin will download package from npm repository and load.
    ![bubblechart](https://cloud.githubusercontent.com/assets/1540981/21749717/280aa430-d559-11e6-9209-889a4f86d7e2.gif)

    ### Questions:
    * Does the licenses files need update? yes
    * Is there breaking changes for older versions? no
    * Does this needs documentation? yes

    Author: Lee moon soo <moon@apache.org>

    Closes #1842 from Leemoonsoo/ZEPPELIN-1619-rebased and squashes the following commits:

    7c49bbb [Lee moon soo] Let Zeppelin continue to bootstrap on offline
    816bdec [Lee moon soo] Display license of package when enabling
    28fb37d [Lee moon soo] beautifulize helium menu
    295768e [Lee moon soo] fix drag and drop visualization reorder
    bb304db [Lee moon soo] Sort version in decreasing order
    e7f18f1 [Lee moon soo] fix english in docs and labels
    c7b187f [Lee moon soo] Merge branch 'master' into ZEPPELIN-1619-rebased
    4c87983 [Lee moon soo] Merge remote-tracking branch 'apache-github/master' into ZEPPELIN-1619-rebased
    ecd925b [Lee moon soo] Merge remote-tracking branch 'apache-github/master' into ZEPPELIN-1619-rebased
    a92cadd [Lee moon soo] Use minifiable syntax
    cec534c [Lee moon soo] Reduce log message
    f373f1d [Lee moon soo] Ignore removed package
    e18d9a4 [Lee moon soo] Ability to customize order of visualization package display
    cd74396 [Lee moon soo] Add rest api doc
    9de5d6d [Lee moon soo] exclude .npmignore and package.json from zeppelin-web rat check
    08abded [Lee moon soo] exclude package.json from rat check
    661c26b [Lee moon soo] update screenshot and keep experimental tag only in docs
    4515805 [Lee moon soo] Display multiple versions of a package
    408c512 [Lee moon soo] Make unittest test bundling with proper vis package on npm registry
    fb7a147 [Lee moon soo] display svg icon
    47de6d9 [Lee moon soo] Propagate bundle error to the front-end
    0fe5e00 [Lee moon soo] visualization development mode
    022e8f6 [Lee moon soo] exclude zeppelin-examples/zeppelin-example-horizontalbar/package.json file from rat check
    2ef3b69 [Lee moon soo] Add new dependency license
    f943d33 [Lee moon soo] Add doc
    f494dbd [Lee moon soo] package npm dependency module in binary package
    b655fa6 [Lee moon soo] use any version of dependency in example. so zeppelin version bumpup doesn't need to take care of them
    2aec52d [Lee moon soo] show loading indicator while enable/disable package
    6c380f6 [Lee moon soo] refactor code to fix HeliumTest
    e142336 [Lee moon soo] update unittest
    7d5e0ae [Lee moon soo] Resolve dependency conflict
    c50a524 [Lee moon soo] Add conf/helium.json in .gitignore
    1c7b73a [Lee moon soo] add result.css
    d2823ad [Lee moon soo] load visualization and tabledata module from source instead npm if accessible
    4e1b061 [Lee moon soo] Convert horizontalbar to VISUALIZATION example
    a5a935b [Lee moon soo] connect visualization factory with restapi
    4b21252 [Lee moon soo] initial implementation of helium menu
    0c4da2e [Lee moon soo] pass bundled visualization to result.controller.js
    f5ce99e [Lee moon soo] import helium service js
    1663582 [Lee moon soo] initial implementation of helium menu
    74d52d4 [Lee moon soo] bundle visualization package from npm repository on the fly