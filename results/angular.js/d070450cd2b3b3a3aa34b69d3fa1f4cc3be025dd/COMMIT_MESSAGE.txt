commit d070450cd2b3b3a3aa34b69d3fa1f4cc3be025dd
Author: Karl Seamon <bannanafiend@gmail.com>
Date:   Tue Dec 3 19:16:08 2013 -0500

    chore(Scope): short-circuit after dirty-checking last dirty watcher

    Stop dirty-checking during $digest after the last dirty watcher has been re-checked.

    This prevents unneeded re-checking of the remaining watchers (They were already
    checked in the previous iteration), bringing a substantial performance improvement
    to the average case run time of $digest.

    Closes #5272
    Closes #5287