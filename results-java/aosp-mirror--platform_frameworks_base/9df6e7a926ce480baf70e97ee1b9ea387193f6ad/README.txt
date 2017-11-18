commit 9df6e7a926ce480baf70e97ee1b9ea387193f6ad
Author: Jeff Brown <jeffbrown@google.com>
Date:   Thu Apr 5 11:49:26 2012 -0700

    Initial commit of InputManager and keyboard layout API.

    Added a new InputManager service for interacting with input
    devices and configuring them.  This will be the focus of
    an upcoming refactoring.

    Added an API for registering keyboard layouts with the system
    based on the use of a broadcast receiver.  Applications can
    register their own keyboard layouts simply by declaring a
    broadcast receiver in their manifests.

    Added the skeleton of a package that will ultimately contain
    the keyboard layouts and other input device related resources
    that are part of the base system.

    Bug: 6110399
    Change-Id: Ie01b0ef4adbd5198f6f012e73964bdef3c51805c