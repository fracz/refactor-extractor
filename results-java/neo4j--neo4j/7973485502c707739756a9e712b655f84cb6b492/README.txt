commit 7973485502c707739756a9e712b655f84cb6b492
Author: Rickard Öberg <rickardoberg@gmail.com>
Date:   Fri Jul 10 15:33:03 2015 +0800

    Optimizes set operations by allowing NodeItem instead of id to go through operations.

    By doing the lookup of node/rel in OperationsFacade once, and then pass it into the Kernel API operations, we can avoid multiple lookups of NodeRecord/RelationshipRecord, and make it easier to access related information through use of cursors from that.

    The cursor interfaces have been refactored from e.g. NodeCursor to Cursor<NodeItem>, to more resemble the traditional iterator/stream pattern. This allows handling of the item itself (e.g. NodeItem) without giving code access to the cursor itself.

    Seek methods have been removed, and instead specifying e.g. which property or label to lookup is done when acquiring the cursor. This makes all cursors look the same, with the same basic methods.

    Rewrote mandatory property constraint check to be more efficient. Should be O(n+m) instead of O(n*m) complexity now.