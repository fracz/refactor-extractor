commit ce2ef766cad1bb186ea522f76c4ac6a8bb3dfa87
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Sep 20 11:39:14 2010 -0700

    Some battery improvements:

    - New API for iterating over history that will allow a better implementation
      in the future.
    - Now do writes asynchronously.

    Also improve the documentation for Activity.onRetainNonInstanceState().

    Change-Id: Idf67f2796a8868eb62f288bcbb2bad29876c8554