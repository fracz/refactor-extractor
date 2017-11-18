commit d6528d34d2e7e8524ef6de11a64eb6ec16ff28aa
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Thu Mar 6 16:45:48 2014 +0400

    vcs notification structure refactored and common integration enabler  provided f
    or mercurial vcs IDEA-120440

    *vcsNotifier used for create and show all vcs notifications;
    *only one method overridden for appropriate TestVcsNotifier;
    *notifications returned as result of all notification methods for external expire-ability;
    *git integration enabler test changed;
    *getNotificator method removed from PlatformFacade;
    *PlatformFacade removed from parameters if possible;
    *annotations added