commit 7b50b2fbdc73e5bebc9b07bfe1175f347fe3060e
Author: epriestley <git@epriestley.com>
Date:   Mon Jun 25 15:21:48 2012 -0700

    Use subprocess output streaming to improve performance of Git commit discovery

    Summary:
    Improve performance of large discovery tasks in Git by using subprocess streaming, like we do for Mercurial.

    Basically, we save the cost of running many `git log` commands by running one big `git log` command but only parsing as much of it as we need to. This is pretty complicated, but we more or less need it for mercurial (which has ~100ms of 'hg' overhead instead of ~5ms of 'git' overhead) so we're already committed to most of the complexity costs. The git implementation is much simpler than the hg implementation because we don't need to handle all the weird parent rules (git gives us to them easily).

    Test Plan:
    Before, `discover --repair` on Phabricator took 35s:

      real  0m35.324s
      user  0m13.364s
      sys   0m21.088s

    Now 7s:

      real  0m7.236s
      user  0m2.436s
      sys   0m3.444s

    Note that most of the time is spent inserting rows after discover, the actual speedup of the git discovery part is much larger (subjectively, it runs in less than a second now, from ~28 seconds before).

    Also ran discover/pull on single new commits in normal cases to verify that nothing broke in the common case.

    Reviewers: jungejason, nh, vrana

    Reviewed By: vrana

    CC: aran

    Maniphest Tasks: T1401

    Differential Revision: https://secure.phabricator.com/D2851