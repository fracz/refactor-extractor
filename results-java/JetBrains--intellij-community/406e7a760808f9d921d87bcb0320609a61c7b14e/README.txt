commit 406e7a760808f9d921d87bcb0320609a61c7b14e
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Wed Oct 23 15:46:13 2013 +0400

    [log] refactor: split loadFirstPart in 2 methods to simplify the logic

    Instead of using complex conditions inside loadFirstPart
    (and they will become more complex when loading more details will be
    needed for filtering implementation),
    introduce two separate method for 2 different procedures:

    * smartRefresh which loads some recent commits unordered, orders them
      and connects to the existing log structure via the VcsLogJoiner.

    * loadFromVcs which loads the given number of recent commits ordered,
      and substitutes the list of top commits.

      The latter should be used only for initialization procedure,
      when the whole log is not ready, and for reading more commits which
      will be needed for filtering.

      It is assumed that if loadFromVcs is called after the whole
      log skeleton has been initialized, it doesn't receive new commits
      that doesn't exist in the whole log.

      However, if that happens, it shouldn't break the subsequent
      smartRefresh: the joiner will just connect new commits to an older
      log structure
      (and request more commits if the given patch is not enough).

    Additionally, move runInBackground() call out from the refreshing
    methods to simplify them, and the calling method execute several
    operations on the LogData synchronously (in a single queue task).