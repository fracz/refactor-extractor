commit 826af6266c79d76b81fe850b9cd3d351597e4adb
Author: Eric Rowe <erowe@google.com>
Date:   Wed Sep 22 19:06:14 2010 -0700

    Modify bluetooth test cases GB for new HC APIs

    Modify bluetooth stress tests pairing and connection test cases for new
    honeycomb bluetooth APIs and severly refactor code, including better
    organization of broadcast receivers, using broadcast receivers to record
    the time (instead of getting the time at the end of the poll).

    Change-Id: I3ef28d54d1a013697f67f4c7c8a96aaadcc747d9