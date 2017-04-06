commit c09773519650d5f1317146b5c996b184876cbeda
Author: kimchy <kimchy@gmail.com>
Date:   Fri Mar 4 20:05:52 2011 +0200

    improve async merge process, don't spawn a thread unless a merge is really needed, and add an optimized "maybeMerge" operation