commit 3e122f76f1648a1ad87f7492c6c491d5dfaf54ea
Author: Tyler Gunn <tgunn@google.com>
Date:   Mon Jan 11 19:25:00 2016 -0800

    Add KEY_USE_RCS_PRESENCE_BOOL carrier config option.

    - New carrier config option is used to determine if presence is used
    to determine whether a contact is capable of video calling.
    - Also, improve logging for PhoneAccount capabilities.

    Bug: 20257833
    Change-Id: Ifcc7df95677eb4399f08eb8849c4004892957e90