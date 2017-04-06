commit 7683166f262f06357a5bed980fe801c19cabb8b6
Author: Chirayu Krishnappa <chirayu@chirayuk.com>
Date:   Wed Apr 15 13:36:46 2015 -0700

    chore(bower): minor refactor to DRY

    The REPOS list was duplicated in publish.sh and unpublish.sh but had
    different orderings of the repos.  This commit consolidates the list
    into a common include file so that they are always in sync.  We could
    improve the scripts a lot more but that's not in the current scope (this
    is all I need to scratch my current itch.)

    Closes #11605