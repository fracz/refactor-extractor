commit 9fde92f9f1f1e40ceb6b3d5ff9676ba590385825
Author: Aditya Auradkar <aauradkar@linkedin.com>
Date:   Mon Oct 12 12:22:39 2015 -0700

    KAFKA-2443 KAFKA-2567; Expose windowSize on Rate; - Throttle time should not return NaN

    This is a followup ticket from KAFKA-2084 to improve the windowSize calculation in Quotas. I've made the following changes:

    1. Added a windowSize function on Rate
    2. Calling Rate.windowSize in ClientQuotaManager to return the exact window size to use when computing the delay time.
    3. Changed the window size calculation subtly. The current calculation had a bug wherein, it used the number of elapsed seconds from the "lastWindowSeconds" of the most recent Sample object. However, the lastWindowSeconds is the time when the sample is created.. this causes an issue because it implies that the current window elapsed time is always "0" when the sample is created. This is incorrect as demonstrated in a testcase I added in MetricsTest. I've fixed the calculation to count the elapsed time from the "oldest" sample in the set since that gives us an accurate value of the exact amount of time elapsed

    Author: Aditya Auradkar <aauradkar@linkedin.com>

    Reviewers: Ismael Juma <ismael@juma.me.uk>, Joel Koshy <jjkoshy.w@gmail.com>

    Closes #213 from auradkar/K-2443