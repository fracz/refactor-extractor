commit 3d9ef196c659df2dc8fb970ac2557f74aa0406f7
Author: Zhang Yuhan <yzhang@yzhang-mbp113.edmunds.hq>
Date:   Tue Oct 21 19:05:29 2014 -0700

    refactor, and added test case for verifying the thread pool properties. problem: metricsRollingStatisticalWindowInMilliseconds, and metricsRollingStatisticalWindowBuckets wouldn't be set correctly