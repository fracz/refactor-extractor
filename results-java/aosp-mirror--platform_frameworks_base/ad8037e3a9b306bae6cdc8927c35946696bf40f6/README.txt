commit ad8037e3a9b306bae6cdc8927c35946696bf40f6
Author: Omari Stephens <xsdg@android.com>
Date:   Tue Mar 13 21:52:13 2012 -0700

    Avoid crashes in a single app from causing cascading test failures

    This should work properly for crashes.  It currently doesn't do the right
    thing for ANRs since, in a lot of cases, they seem to happen asynchronously
    _after_ the testcase has ended.  Will try to improve that behavior with a
    subsequent change.

    Bug: 6128185
    Change-Id: Ie535141e879062c11ee7108b37d282a33a5b5eef