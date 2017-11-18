commit e535a5827b7b7c7c70f4701d74051c9bb5eb0b98
Author: Srinath Sridharan <srinathsr@google.com>
Date:   Mon Jun 27 18:13:47 2016 -0700

    Add a new API to improve VR thread scheduling.

    Adds a new API that enables device-specific scheduler optimizations for
    latency-sensitive VR threads.

    BUG: 29163534
    Change-Id: I58d7be0eb266eca452c804cd07004784fb7daf2b