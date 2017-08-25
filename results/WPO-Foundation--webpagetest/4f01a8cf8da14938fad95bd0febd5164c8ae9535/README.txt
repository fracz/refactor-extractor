commit 4f01a8cf8da14938fad95bd0febd5164c8ae9535
Author: Matthew Lee <mgl@google.com>
Date:   Wed May 28 19:45:27 2014 -0700

    Refactor graph_page_data

    Remove mean function

    Add comments for refactor.

    Add more comments

    Add more comments, refactor GetMedianRun, and make successfulOnly a required parameter to values.

    Add test to graphPageDataTest.php

    Add test to graphPageDataTest.php

    More documentation

    Move GetQuantiles and GetNumeric to graph_page_data.inc

    Finish moving GetQuantiles and GetNumeric to graph_page_data.inc

    More updates to test

    Test pageload data error

    Merge some changes from https://github.com/geening/webpagetest/commit/936dba606a072f26b727b3fcbf96bfc0620c842a

    Update test load page data

    removing test_load_page_data

    Code cleanup of graph_page_data.php

    Add GetMedianRun test

    Fixing code review issues in https://codereview.appspot.com/100870045/

    Refactor graph_page_data.php to make it easier to combine multiple tests
    onto
    one chart.
    Show median metric value in addition to median run.
    Extract commonly used functionality in page_data.inc into "values" and
    "successfulRun" functions.
    Fix some typos and add comments.

    Remove values corresponding to failed runs from tests -- see differences
    at
    http://latencylab/graph_page_data.php?test=140529_V9_35C
    http://latencylab/graph_page_data.php?test=140530_NR_318