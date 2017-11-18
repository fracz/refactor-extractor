commit e0cdb6079e9e9e8b87a71d67ef5aa1dc0e3e6840
Author: Jason Monk <jmonk@google.com>
Date:   Wed Nov 5 12:39:45 2014 -0500

    Prepare for testing the NetworkControllerImpl

    This will allow us to add some test cases to verify that under
    certain phone/signal conditions we get out the icons we expect.
    This will let us break less things when refactoring for MSIM.

    Bug: 18222975
    Change-Id: I7bd3e66e7de6b30fede72e40fb6aa37dd523336c