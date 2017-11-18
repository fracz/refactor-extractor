commit d34dc4bc3125034b5d83c7af143b29b1301108ac
Author: Haozhun Jin <haozhun.jin@gmail.com>
Date:   Sun Mar 19 15:04:14 2017 -0700

    Minor improvements to hive tests

    * Use VARCHAR(HiveVarchar.MAX_VARCHAR_LENGTH) instead of VARCHAR when the former is accurate
    * Remove unnecessary copyRegion in blockToSlicee