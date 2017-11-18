commit e5e3d910179b538ede3ae609562c4d009d688b78
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Sun Mar 1 18:08:00 2015 +0300

    [log] basic support negated branches for git & hg providers

    Actually, no filtering by branch is done here, i.e. commits returned by
    the VcsLogProvider unmatch the given excluded branch filter.
    Branches are filtered by the VisiblePackBuilder, so there will be no
    extra commits.

    The downside is that the first pack of commits returned by the
    VcsLogProvider can be in reality very small when additionally filtered
    by branch => an extra request to the VCS will be needed.

    Git itself doesn't support such a filter, so this can't be fixed there.
    Hg probably does, so the filtering method can be improved there.