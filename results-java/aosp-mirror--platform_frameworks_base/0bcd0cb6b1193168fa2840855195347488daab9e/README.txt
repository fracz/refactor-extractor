commit 0bcd0cb6b1193168fa2840855195347488daab9e
Author: Thomas Buhot <thomas.buhot@intel.com>
Date:   Fri Dec 4 12:18:03 2015 +0100

    libhwui: make setSurface asynchronous

    On the critical path of the cold launch of applications
    the main thread of the started application tells the RenderThread
    to create a surface. This process is synchronous and blocks
    the main thread of the application until the creation
    of the EGLContext is complete.
    As a consequence the launch time of the application is delayed
    by time spent allocating the EGL Context in the RenderThread.

    With this optimization the launch time of any application
    is improved (for example settings by 20 to 40 ms).

    Change-Id: Ibf47aaa0abb8dedf7aa00693073db3785d9d6b08
    Signed-off-by: Thomas Buhot <thomas.buhot@intel.com>
    Signed-off-by: Zhiquan Liu <zhiquan.liu@intel.com>