commit fbd41e42190ff8bf11222a66bf787a6a38a38208
Author: vrana <jakubv@fb.com>
Date:   Thu Feb 21 03:10:02 2013 -0800

    Move revisions without reviewers to author's queue

    Summary:
    If the revision doesn't have reviewers then it's really not waiting on someone else and the author must take an action.
    An improvement would be to check if the reviewers are not disabled but that would require loading their handles.

    Test Plan:
    /
    /differential/

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: aran, Korvin, s.o.butler

    Differential Revision: https://secure.phabricator.com/D5046