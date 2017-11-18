commit d2ed9d01c89d773a0a5088fe785fdcc793f915f5
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Sep 4 10:55:44 2012 -0700

    Fix issue #7097984 java.lang.SecurityException: Permission Denial:

    broadcast asks to run as user -1 but is calling from user 0; this requires

    Also improve part of issue #7087789: Local denial of service via
    low-permissioned apps

    No longer allow closeSystemDialogs() from background processes.

    Change-Id: I752d5a1d51be0b69fde6999d6659835e5bde3efe