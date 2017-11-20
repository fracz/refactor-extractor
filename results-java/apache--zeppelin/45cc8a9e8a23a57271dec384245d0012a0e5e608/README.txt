commit 45cc8a9e8a23a57271dec384245d0012a0e5e608
Author: 1ambda <1amb4a@gmail.com>
Date:   Tue Apr 11 23:14:46 2017 +0900

    [ZEPPELIN-2217] AdvancedTransformation for Visualization

    ### What is this PR for?

    `AdvancedTransformation` has more detailed options while providing existing features of `PivotTransformation` and `ColumnselectorTransformation` which Zeppelin already has

    ![av_in_30sec](https://cloud.githubusercontent.com/assets/4968473/24037330/c9478e86-0b40-11e7-9886-1ffb85042a7a.gif)

    Here are some features which advanced-transformation can provide.

    1. **(screenshot)** multiple sub charts
    2. **(screenshot)** parameter widgets: `input`, `checkbox`, `option`, `textarea`
    3. **(screenshot)** expand/fold axis and parameter panels
    4. **(screenshot)** clear axis and parameter panels
    5. **(screenshot)** remove duplicated columns in an axis
    6. **(screenshot)** limit column count in an axis
    7. configurable char axes: `valueType`, `axisType`, `description`, ...
    8. configurable chart parameters
    9. lazy transformation
    10. parsing parameters automatically based on their type: `int`, `float`, `string`, `JSON`
    11. multiple transformation methods
    12. re-initialize whole configuration based on spec hash.
    13. **(screenshot)** shared axis

    #### API Details: Spec

    `AdvancedTransformation` requires `spec` which includes axis and parameter details for charts.

    - Let's create 2 sub-charts called `simple-line` and `step-line`.
    - Each sub chart can have different `axis` and `parameter` depending on their requirements.

    ```js
      constructor(targetEl, config) {
        super(targetEl, config)

        const spec = {
          charts: {
            'simple-line': {
              sharedAxis: true, /** set if you want to share axes between sub charts, default is `false` */
              axis: {
                'xAxis': { dimension: 'multiple', axisType: 'key', },
                'yAxis': { dimension: 'multiple', axisType: 'aggregator'},
                'category': { dimension: 'multiple', axisType: 'group', },
              },
              parameter: {
                'xAxisUnit': { valueType: 'string', defaultValue: '', description: 'unit of xAxis', },
                'yAxisUnit': { valueType: 'string', defaultValue: '', description: 'unit of yAxis', },
                'dashLength': { valueType: 'int', defaultValue: 0, description: 'the length of dash', },
              },
            },

            'step-line': {
              axis: {
                'xAxis': { dimension: 'single', axisType: 'unique', },
                'yAxis': { dimension: 'multiple', axisType: 'value', },
              },
              parameter: {
                'xAxisUnit': { valueType: 'string', defaultValue: '', description: 'unit of xAxis', },
                'yAxisUnit': { valueType: 'string', defaultValue: '', description: 'unit of yAxis', },
                'noStepRisers': { valueType: 'boolean', defaultValue: false, description: 'no risers in step line', widget: 'checkbox', },
            },

          },
        }

        this.transformation = new AdvancedTransformation(config, spec)
      }
    ```

    ####  API Details: Axis Spec

    | Field Name | Available Values (type) | Description |
    | --- | --- | --- |
    |`dimension` | `single` | Axis can contains only 1 column |
    |`dimension` | `multiple` | Axis can contains multiple columns |
    |`axisType` | `key` | Column(s) in this axis will be used as `key` like in `PivotTransformation`. These columns will be served in `column.key` |
    |`axisType` | `aggregator` | Column(s) in this axis will be used as `value` like in `PivotTransformation`. These columns will be served in `column.aggregator` |
    |`axisType` | `group` | Column(s) in this axis will be used as `group` like in `PivotTransformation`. These columns will be served in `column.group` |
    |`axisType` | (string) | Any string value can be used here. These columns will be served in `column.custom` |
    |`maxAxisCount` | (int) | The maximum column count that this axis can contains. (unlimited if `undefined`) |
    |`valueType` | (string) | Describe the value type just for annotation |

    Here is an example.

    ```js
              axis: {
                'xAxis': { dimension: 'multiple', axisType: 'key',  },
                'yAxis': { dimension: 'multiple', axisType: 'aggregator'},
                'category': { dimension: 'multiple', axisType: 'group', maxAxisCount: 2, valueType: 'string', },
              },
    ```

    ####  API Details: Parameter Spec

    | Field Name | Available Values (type) | Description |
    | --- | --- | --- |
    |`valueType` | `string` | Parameter which has string value |
    |`valueType` | `int` | Parameter which has int value |
    |`valueType` | `float` | Parameter which has float value |
    |`valueType` | `boolean` | Parameter which has boolean value used with `checkbox` widget usually |
    |`valueType` | `JSON` | Parameter which has JSON value used with `textarea` widget usually. `defaultValue` should be `""` (empty string). This ||`defaultValue` | (any) | Default value of this parameter. `JSON` type should have `""` (empty string) |
    |`description` | (string) | Description of this parameter. This value will be parsed as HTML for pretty output |
    |`widget` | `input` |  Use [input](https://developer.mozilla.org/en/docs/Web/HTML/Element/input) widget. This is the default widget (if `widget` is undefined)|
    |`widget` | `checkbox` |  Use [checkbox](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/checkbox) widget. |
    |`widget` | `textarea` |  Use [textarea](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/textarea) widget. |
    |`widget` | `option` |  Use [select + option](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/select) widget. This parameter should have `optionValues` field as well. |
    |`optionValues` | (Array<string>) |  Available option values used with the `option` widget |

    Here is an example.

    ```js
    parameter: {
      // string type, input widget
      'xAxisUnit': { valueType: 'string', defaultValue: '', description: 'unit of xAxis', },

      // boolean type, checkbox widget
      'inverted': { widget: 'checkbox', valueType: 'boolean', defaultValue: false, description: 'invert x and y axes', },

      // string type, option widget with `optionValues`
      'graphType': { widget: 'option', valueType: 'string', defaultValue: 'line', description: 'graph type', optionValues: [ 'line', 'smoothedLine', 'step', ], },

      // HTML in `description`
      'dateFormat': { valueType: 'string', defaultValue: '', description: 'format of date (<a href="https://docs.amcharts.com/3/javascriptcharts/AmGraph#dateFormat">doc</a>) (e.g YYYY-MM-DD)', },

      // JSON type, textarea widget
      'yAxisGuides': { widget: 'textarea', valueType: 'JSON', defaultValue: '', description: 'guides of yAxis ', },
    ```

    #### API Details: Transformer Spec

    `AdvancedTransformation` supports 3 transformation methods. The return value will depend on the transformation method type.

    ```js
        const spec = {
          charts: {
            'simple': {
              /** default value of `transform.method` is the flatten cube.  */
              axis: { ... },
              parameter: { ... }
            },

            'cube-group': {
              transform: { method: 'cube', },
              axis: { ... },
              parameter: { ... },
            }

            'no-group': {
              transform: { method: 'raw', },
              axis: { ... },
              parameter: { ... },
            }
    ```

    | Field Name | Available Values (type) | Description |
    | --- | --- | --- |
    |`method` | `object` |  designed for [amcharts: serial](https://www.amcharts.com/demos/date-based-data/) |
    |`method` | `array` | designed for [highcharts: column](http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/demo/column-basic/) |
    |`method` | `drill-down` | designed for [highcharts: drill-down](http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/demo/column-drilldown/) |
    |`method` | `raw` | will return the original `tableData.rows` |

    Whatever you specified as `transform.method`, the `transformer` value will be always function for lazy computation.

    ```js
    // advanced-transformation.util#getTransformer

      if (transformSpec.method === 'raw') {
        transformer = () => { return rows; }
      } else if (transformSpec.method === 'array') {
        transformer = () => {
          ...
          return { ... }
        }
      }

    ```

    #### Feature Details: Automatic parameter parsing

    Advanced transformation will parse parameter values automatically based on their type: `int`, `float`, `string`, `JSON`

    - See also `advanced-transformation-util.js#parseParameter`

    #### Feature Details: re-initialize the whole configuration based on spec hash

    ```js
    // advanced-transformation-util#initializeConfig

      const currentVersion = JSON.stringify(spec)
      if (!config.spec || !config.spec.version || config.spec.version !== currentVersion) {
        spec.version = currentVersion
        // reset config...
      }
    ```

    #### Feature Details: Shared Axes

    If you set `sharedAxis` to `true` in chart specification, then these charts will share their axes. (default is `false`)

    ```js
        const spec = {
          charts: {
            'column': {
              transform: { method: 'array', },
              sharedAxis: true,
              axis: { ... },
              parameter: { ... },
            },

            'stacked': {
              transform: { method: 'array', },
              sharedAxis: true,
              axis: { ... }
              parameter: { ... },
            },
    ```

    ![sharedaxis](https://cloud.githubusercontent.com/assets/4968473/24207116/6999ad8a-0f63-11e7-8b61-273b712612fc.gif)

    #### API Details: Usage in Visualization#render()

    Let's assume that we want to create 2 sub-charts called `basic` and `no-group`.

    - https://github.com/1ambda/zeppelin-ultimate-line-chart (an practical example)

    ```js
      drawBasicChart(parameter, column, transformer) {
        const { ... } = transformer()
      }

      drawNoGroupChart(parameter, column, transformer) {
        const { ... } = transformer()
      }

      render(data) {
        const { chart, parameter, column, transformer, } = data

        if (chart === 'basic') {
          this.drawBasicChart(parameter, column, transformer)
        } else if (chart === 'no-group') {
          this.drawNoGroupChart(parameter, column, transformer)
        }
      }
    ```

    ### What type of PR is it?
    [Feature]

    ### Todos

    NONE

    ### What is the Jira issue?

    [ZEPPELIN-2217](https://issues.apache.org/jira/browse/ZEPPELIN-2217)

    ### How should this be tested?

    1. Clone https://github.com/1ambda/zeppelin-ultimate-line-chart
    2. Create a symbolic link `ultimate-line-chart.json` into `$ZEPPELIN_HOME/helium`
    3. Modify the `artifact` value to proper absolute path considering your local machine.
    4. Install the above visualization in `localhost:9000/#helium`
    5. Test it

    ### Screenshots (if appropriate)

    #### 1. *(screenshot)* multiple sub charts

    ![av_multiple_charts](https://cloud.githubusercontent.com/assets/4968473/24034638/7b84dba0-0b35-11e7-989d-059ccc87f968.gif)

    #### 2. *(screenshot)* parameter widgets: `input`, `checkbox`, `option`, `textarea`

    ![av_widgets_new](https://cloud.githubusercontent.com/assets/4968473/24034652/88679d6c-0b35-11e7-835a-3970d7124850.gif)

    #### 3. *(screenshot)* expand/fold axis and parameter panels

    ![av_fold_expand](https://cloud.githubusercontent.com/assets/4968473/24034653/8a634ddc-0b35-11e7-9851-15280a6b5fd3.gif)

    #### 4. *(screenshot)* clear axis and parameter panels

    ![av_clean_buttons](https://cloud.githubusercontent.com/assets/4968473/24034654/8d3dc14a-0b35-11e7-98c7-3aeddce6d80a.gif)

    #### 5. *(screenshot)* remove duplicated columns in an axis

    ![av_duplicated_columns](https://cloud.githubusercontent.com/assets/4968473/24034657/910f4d20-0b35-11e7-9e9b-d9e2f799a5dd.gif)

    #### 6. *(screenshot)* limit column count in an axis

    ![av_maxaxiscount](https://cloud.githubusercontent.com/assets/4968473/24034679/a5e8eb34-0b35-11e7-89cd-070f3790d511.gif)

    ### Questions:
    * Does the licenses files need update? - NO
    * Is there breaking changes for older versions? - NO
    * Does this needs documentation? - NO

    Author: 1ambda <1amb4a@gmail.com>

    Closes #2098 from 1ambda/ZEPPELIN-2217/advanced-transformation and squashes the following commits:

    6cde7c9 [1ambda] fix reset params when spec change
    c75a3f2 [1ambda] fix: Reset persisted axis
    6a2130a [1ambda] fix: clear config only when axis changed
    5464e84 [1ambda] fix: Optimize array 2 key method
    9beb1e7 [1ambda] fix: Type error
    2408225 [1ambda] test: Add test for array 2key
    bf56761 [1ambda] feat: Add array:2-key transform method
    7c6768f [1ambda] feat: Use axisSpec.desc as tooltip
    f98d4c9 [1ambda] fix: Remove invalid key  prop
    5cf2ece [1ambda] feat: Add minAxisCount
    4887800 [1ambda] fix: Remove local module yarn caches
    3e29572 [1ambda] refactor: copyModule func
    c91a033 [1ambda] fix: Set yarn cache dir in helium-bundles
    04b5140 [1ambda] fix: Import a-tr
    0a876cf [1ambda] docs: Update index.md
    380b1af [1ambda] docs: Fix typo and add desc for existing trs
    908214b [1ambda] docs: Move experimental tags
    a009627 [1ambda] feat: Allow dup aggr axis
    3b44e92 [1ambda] fix: Remove unuse const
    ab6c22e [1ambda] test: Add test for drill-down method
    756107a [1ambda] test: Add array transformation method
    d819c73 [1ambda] test: Add object method
    bf00fba [1ambda] test: Add MockTableData
    39fe5ae [1ambda] test: Add test for getColumnsFromAxis
    4c393b4 [1ambda] fix: Add polyfill for es6 funcs in test
    e92c787 [1ambda] test: Add test for rmDup, aplMaxAxisCount
    843f45d [1ambda] test: Add test for getCurrent* funcs
    ae5277c [1ambda] test: Add test for initializeConfig
    c14a9dc7 [1ambda] test: Add tests for widget, params
    c510af1 [1ambda] docs: Add doc for Transformation
    52db37b [1ambda] feat: Show panel menus only when opened
    17ad4a4 [1ambda] feat: Support chartChanged, parameterChanged
    c0d33d3 [1ambda] fix: Sort selectors in drilldown method
    cfd6fef [1ambda] feat: sharedAxis
    9af80ce [1ambda] style: Indent
    79b5654 [1ambda] fix: return the same info in transform
    7bee464 [1ambda] fix: Keynames
    ee8788e [1ambda] feat: Support drill-down
    666025a [1ambda] fix: DON'T reset current chart
    ae1891f [1ambda] add array:key transform
    4167a2e [1ambda] fix: Sort keyNames
    912b5b7 [1ambda] fix: Persist initialized config
    f1f6b0c [1ambda] feat: Support ARRAY transform.method
    812f9a2 [1ambda] fix: Set proper aggr value when 0 group
    20f9437 [1ambda] fix: getCube func
    25d51a9 [1ambda] DON'T display aggr.name when aggrColumns.length == 1
    f37e13d [1ambda] fix: Add 'object' transform.method
    da2370c [1ambda] fix: Add resetAxis, Param funcs
    2370682 [1ambda] fix: average is not caculated correctly
    dd08e38 [1ambda] fix: Set param panel height to 400
    881695a [1ambda] feat: clear chart, param separately
    4d0d62b [1ambda] fix: DON'T clean panel config
    92676d1 [1ambda] fix: limit parameter panel height to 370
    cc29060 [1ambda] feat: parse param description as HTML
    9a2d227 [1ambda] fix: Stop event propagation in widgets
    fcc625c [1ambda] feat: Automatic param parsing
    b4d774c [1ambda] fix: Dont close param panel when enter
    088705b [1ambda] refactor: Remove util and add Widget funcs
    bf88b4f [1ambda] feat: textare widget and update hook
    4e73012 [1ambda] feat: widget checkbox
    11b7eaa [1ambda] feat: option widget
    5d3efc9 [1ambda] fix: Change panel header
    b1d9d31 [1ambda] feat: Save and close with enter key
    53f508c [1ambda] feat: custom axisSpec
    0dbc431 [1ambda] feat: Support transformer
    94d837a [1ambda] feat: Automatic spec versioning
    74b8b4e [1ambda] fix: Duplicated radio btn id, name
    5b88f08 [1ambda] fix: Modify margin of subchart radio btns
    019892c [1ambda] feat: Support transform: flatten
    0484e1e [1ambda] feat: Support maxAxisCount in axisSpec
    936901b [1ambda] feat: Support undefined valueType in axisSpec
    7a454ff [1ambda] feat: Cube Transformation
    f0ed02f [1ambda] feat: Support same axis types
    49985c6 [1ambda] refactor: Refine axis, param spec
    d89e223 [1ambda] feat: advanced-transformation-api
    75569ce [1ambda] feat: Support multiple charts in UI
    e1fcc2e [1ambda] feat: Support multiple charts
    97be629 [1ambda] fix: Add singleDimensionAggregatorChanged
    676bd7e [1ambda] refactor: Refine transform API
    9fb398e [1ambda] feat: Add clearConfig
    a8a4fb1 [1ambda] refactor: Add getAxisInSingleDimension func
    9768ecf [1ambda] feat: Add groupBase axis option
    91ae54d [1ambda] fix: Overflow issue in single aggr
    10c80fc [1ambda] feat: AdvancedTransformation