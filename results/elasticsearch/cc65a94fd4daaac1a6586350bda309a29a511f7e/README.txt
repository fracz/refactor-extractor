commit cc65a94fd4daaac1a6586350bda309a29a511f7e
Author: Luca Cavanna <javanna@users.noreply.github.com>
Date:   Thu Mar 2 12:43:20 2017 +0100

    [TEST] improve yaml test sections parsing (#23407)

    Throw error when skip or do sections are malformed, such as they don't start with the proper token (START_OBJECT). That signals bad indentation, which would be ignored otherwise. Thanks (or due to) our pull parsing code, we were still able to properly parse the sections, yet other runners weren't able to.

    Closes #21980

    * [TEST] fix indentation in matrix_stats yaml tests

    * [TEST] fix indentation in painless yaml test

    * [TEST] fix indentation in analysis yaml tests

    * [TEST] fix indentation in generated docs yaml tests

    * [TEST] fix indentation in multi_cluster_search yaml tests