commit 0b4834f7da2486848cbdc3b1319954c4700aea89
Author: Ryan Ernst <ryan@iernst.net>
Date:   Thu Feb 23 11:19:08 2017 -0800

    Test: Fix hdfs test fixture setup on windows

    The test setup for hdfs is a little complicated for windows, needing to
    check if the hdfs fixture can be run at all. This was unfortunately not
    updated when the integ tests were reorganized into separate runner and
    cluster setups.