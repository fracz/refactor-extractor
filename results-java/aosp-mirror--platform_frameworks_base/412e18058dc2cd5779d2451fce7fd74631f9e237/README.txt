commit 412e18058dc2cd5779d2451fce7fd74631f9e237
Author: Winson <winsonc@google.com>
Date:   Tue Oct 20 16:57:57 2015 -0700

    Additional refactoring interface between component and activity.

    - Removing broadcasts for communicating with the Recents activity from
      the component in favor of using events.

    Change-Id: I2ddfde911bd1fd1b2d63bb84a0e7f0338f955df6