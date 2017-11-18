commit bba0030a8529fd904056cbb7cfbbe3afbc1c8a95
Author: Adam Powell <adamp@google.com>
Date:   Wed Feb 3 16:01:09 2016 -0800

    Restrict ChooserTargets provided by a single service

    Only allow one row's worth of targets from any one
    ChooserTargetService and lower the weight for app recency in resolver
    sorting. Along with a previous change to only consider the past 1 week
    of app activity, this is to help improve the quality of direct share
    targets shown.

    Bug 26562857

    Change-Id: I0f9a8ca8ccfb655261421e29fef7909cadd318f1