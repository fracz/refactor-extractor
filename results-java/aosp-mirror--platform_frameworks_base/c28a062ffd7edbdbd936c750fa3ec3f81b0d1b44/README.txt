commit c28a062ffd7edbdbd936c750fa3ec3f81b0d1b44
Author: Guang Zhu <guangzhu@google.com>
Date:   Sat Dec 1 23:37:57 2012 -0800

    app launch test fixes and improvements

    * fixed NPE when specified app name does not exist
    * force stop package before starting, because some names may
      resolve into the same package
    * ensure app is launched in the order as sepcified in command
      line
    * fixed time recording: it should have been 'thisTime' as
      reported by ActivityManager, to be consistent with previous
      harness

    Change-Id: I411a568580feef21821dcbe6ec15884f697af6fd