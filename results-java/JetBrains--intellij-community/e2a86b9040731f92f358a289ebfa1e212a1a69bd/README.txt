commit e2a86b9040731f92f358a289ebfa1e212a1a69bd
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Wed Sep 18 20:51:04 2013 +0400

    [log] Multiple fixes and refactorings in VcsLogDataHolder

    * Precisely define refresh procedure and write in down in the javadoc.
    * Keep the full log separated by roots, and together with references.
    * Provide old references to the VcsLogJoiner.
    * Remember if the user was looking at the whole log or only at the
      top part of it - to restore this state after refresh.