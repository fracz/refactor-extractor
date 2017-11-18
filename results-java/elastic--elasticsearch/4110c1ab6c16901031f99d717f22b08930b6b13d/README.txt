commit 4110c1ab6c16901031f99d717f22b08930b6b13d
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Sun Mar 1 12:10:35 2015 +0100

    Test: InternalEngineTests.testSegmentsWithMergeFlag should close it's translog

    Also improve test suite tearDown to not fail if setUp didn't create engines, translogs etc.