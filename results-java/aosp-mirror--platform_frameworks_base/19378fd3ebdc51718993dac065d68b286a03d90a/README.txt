commit 19378fd3ebdc51718993dac065d68b286a03d90a
Author: Jason Monk <jmonk@google.com>
Date:   Mon Mar 27 15:45:32 2017 -0400

    Don't auto-mirror QS drawables

    Was a mistake in previous refactor, they were not being mirrored
    before.

    Test: visual
    Change-Id: I001757ca02267e33409a9c0071a24b69939f30c5
    Fixes: 36255688
    Fixes: 36255681