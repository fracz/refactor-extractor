commit f57bfe2fefc87fdb1dcc27b0f4b3a11996c15da2
Author: Doris Liu <tianliu@google.com>
Date:   Thu Oct 1 13:26:01 2015 -0700

    Fix behavior change for animators without a start delay

    This CL maintains the behavior that animators without any
    start delay will get started right away, instead of being delayed
    to the first frame as it was in the previous refactor.

    Bug: 23825781
    Change-Id: I7873bc6c7a761c1b4d48ee5e17af631b359fd676