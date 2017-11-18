commit 2183ba9b5f8eb54c74cd198a7506b21319794e99
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Wed Apr 5 14:06:11 2017 +0900

    Nsdmanager/NsdService: add logging

    This patch adds basic logging to NsdManager and NsdService, and improves
    the facilities for pretty printing the event ids defined in NsdManager.

    It also includes a few minor cleanups:
      - adding 'final' on effectively final instance variables of NsdManager
      and NsdService.
      - similarly, adding 'static' on effectively static class fields.
      - regrouping instance variables together.

    Test: no functional changes
    Bug: 33074219
    Change-Id: I360d539e73cc8e4b45d4e0d20b2e345455fdb10c