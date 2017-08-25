commit a5bd848ccc5e533e3c755b2bbb37d636be1a717f
Author: Borek Bernard <borekb@gmail.com>
Date:   Thu Apr 21 16:12:06 2016 +0200

    Slightly better implementation of makeDraftFromUnsavedPost() that does not depend on `sleep()`.

    It's not a fix for a "bug" in #957 but the improvement was done when inspecting the possible issue.