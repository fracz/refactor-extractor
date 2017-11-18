commit 12247d566317d98273d78321fd3ed21ca4ffc70f
Author: Raghav Sethi <raghavsethi@fb.com>
Date:   Wed Feb 10 15:25:43 2016 -0800

    Fix Hive partition column validation

    The previous approach to validation presented the user with a
    difficult to understand error message and made one code path
    unreachable.

    Also improved error message for missing partition columns.