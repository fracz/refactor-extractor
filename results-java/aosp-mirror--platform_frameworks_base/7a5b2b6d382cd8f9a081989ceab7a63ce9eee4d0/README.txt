commit 7a5b2b6d382cd8f9a081989ceab7a63ce9eee4d0
Author: Mady Mellor <madym@google.com>
Date:   Fri Apr 14 18:53:45 2017 -0700

    Fix issue where the notification could snap to the menu while its invisible

    1) If the snap back animation is cancelled, the menu shouldn't be reset
       because it hasn't actually been snapped back -- this could happen when
       the menu was being shown and then the icon alpha would get set to 0
       resulting in notification snapped to menu but no menu visible
    2) Cancel the callback to fade the menu in on dismiss to match original
       behavior before refactor of NMR

    Test: manual - no clear repro steps for this, I could get it by
          violently moving a notification back and forth, with this patch
          I haven't been able to repro it.
    Bug: 37320279
    Change-Id: I4eea37e3c454e7324d0e295b0ec2fe022d219253