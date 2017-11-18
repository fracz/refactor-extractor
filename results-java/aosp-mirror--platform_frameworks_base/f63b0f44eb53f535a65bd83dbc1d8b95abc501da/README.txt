commit f63b0f44eb53f535a65bd83dbc1d8b95abc501da
Author: Joe Onorato <joeo@google.com>
Date:   Sun Sep 12 17:03:19 2010 -0400

    Connect my plumbing to dsandler's awesome lights out mode.

    It took a little bit of refactoring to move the authoritative state
    about whether the lights are on or not into the StatusBarManagerService,
    so that if the system ui process crashes, the bar comes up in the
    right mode.

    Change-Id: I95cfaf8f78ca4443ded5262272ea755d44dc5d17