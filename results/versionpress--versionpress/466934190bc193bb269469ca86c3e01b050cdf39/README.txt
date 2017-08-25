commit 466934190bc193bb269469ca86c3e01b050cdf39
Author: Borek Bernard <borekb@gmail.com>
Date:   Wed Feb 24 14:34:31 2016 +0100

    [#588] Fixing and refactoring test from 177ea42. Details:

    - The test was not actually testing anything as the posts in both test and clone sites have likely been modified in the same second. `sleep()` was added.
    - Test renamed from `updatedArticleCanBeMergedFromClone()` to `dateModifiedMergesAutomatically()` to better reflect what id does.
    - Some other things slightly refactored.