commit f24f21695f5609d06402cf61e3500d408b99bdcb
Author: Winson <winsonc@google.com>
Date:   Tue Jan 5 12:11:55 2016 -0800

    Refactoring and unifying TaskView animations.

    - Adding notion of a TaskViewAnimation to animate a TaskView to a
      specific TaskViewTransform
    - Refactoring task view enter/exit/launch/delete animations into
      a separate class so that we can improve them easier
    - Removing individual TaskView view property animations in favor
      of using the existing TaskStackView stack animation. This ensures that
      we don't have to add separate logic when animating TaskViews.  It is
      all handled by the TaskStackView now.
    - Breaking down the TaskStackView synchronize method into binding
      TaskViews and updating them to transforms.  This allows us to
      synchronously update in many cases and is cleaner than the many
      request* calls.

    Change-Id: Ib26793568a14e837e6782358155f21158a133992