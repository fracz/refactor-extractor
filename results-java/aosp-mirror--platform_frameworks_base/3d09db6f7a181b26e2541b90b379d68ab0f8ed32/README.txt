commit 3d09db6f7a181b26e2541b90b379d68ab0f8ed32
Author: Tyler Gunn <tgunn@google.com>
Date:   Mon Mar 14 16:06:11 2016 -0700

    DO NOT MERGE Add KEY_USE_RCS_PRESENCE_BOOL carrier config option.

    - New carrier config option is used to determine if presence is used
    to determine whether a contact is capable of video calling.
    - Also, improve logging for PhoneAccount capabilities.

    Bug: 20257833
    Change-Id: Ifcc7df95677eb4399f08eb8849c4004892957e90