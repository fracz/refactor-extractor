commit 9bfb707597898f54722460b48588007b682f3e2a
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Sep 22 11:37:40 2009 -0700

    Various fixes and improvements to window, activity.

    - New meta-data you can add to a dock activity to have it launched by the
      home key when the device is in that dock.

    - Fix a deadlock involving ActivityThread's internal content provider lock.

    - New window flag to have a non-secure keyguard entirely dismissed when a
      window is displayed.

    - New WindowManagerPolicy APIs to allow the policy to tell the system when
      a change it makes during layout may cause the wall paper or
      overall configuration to change.

    - Fix a bug where an application token removed while one of its windows is
      animating could cause the animating window to get stuck on screen.

    Change-Id: I6d33fd39edd796bb9bdfd9dd7e077b84ca62ea08