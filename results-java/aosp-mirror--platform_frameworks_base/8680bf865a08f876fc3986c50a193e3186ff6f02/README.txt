commit 8680bf865a08f876fc3986c50a193e3186ff6f02
Author: Daniel Sandler <dsandler@android.com>
Date:   Tue May 15 16:52:52 2012 -0400

    Action button improvements:

      - Horizontal layout
      - At most 2 are shown
      - Tombstones are now shown (if the intent is null, the
        button is disabled; use it for quick feedback of an
        action's effect)

    Bug: 6418617 (tombstones)
    Bug: 6482237 (action separators)
    Change-Id: Ie0c613006227bbfe1c0ec6eab1cda4f3782a05f2