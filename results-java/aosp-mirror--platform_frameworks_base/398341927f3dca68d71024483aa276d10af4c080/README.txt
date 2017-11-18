commit 398341927f3dca68d71024483aa276d10af4c080
Author: Craig Mautner <cmautner@google.com>
Date:   Sun Sep 2 07:47:24 2012 -0700

    Minor refactors.

    - Refactor DragState to take Display instead of DisplayContent.
    - Rename xxxAnimationLw methods in WindowManagerPolicy to xxxPostLayout
    to reflect animation refactoring.

    Change-Id: I502f2aa45a699ad395a249a12abf9843294623f0