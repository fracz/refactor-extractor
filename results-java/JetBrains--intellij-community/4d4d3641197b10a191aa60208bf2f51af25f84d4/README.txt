commit 4d4d3641197b10a191aa60208bf2f51af25f84d4
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Tue Jan 21 16:28:07 2014 +0400

    [log] refactor: group filters before passing them to providers

    Instead of passing a Collection of VcsLogFilter and thus forcing each
    VcsLogProvider sort filters by type on its own,
    group them in the common code and pass a separate Collection of each
    filter type.

    Extract interfaces for each filter type to let them be used in
    VcsLogProvider.