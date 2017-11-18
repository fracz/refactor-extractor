commit 714fd989fb8f23bc769e1ac6771406538dc50330
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Mon May 28 16:08:47 2012 +0400

    [merge] refactoring: move inner classes outside, move some methods, cleanup, code style

    * Move Change.HighlighterHolder and Change.Side inner classes to outside since they are used a lot from the ouside of the Change class.
    * Move ChangeType.apply() to Change, because it is nothing to do with the ChangeType: it is the Change which is being applied.
    * Add some javadocs.
    * Cleanup.