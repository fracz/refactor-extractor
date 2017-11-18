commit fa9d338f30dfcbce48d5c37613e7a2d230a36032
Author: Kirwan Lyster <kirwanlyster@fb.com>
Date:   Mon Jul 25 13:31:19 2016 -0700

    Don't fail to get cached item due to missing resource index entry

    Summary: With the disk cache index improved, a change was added that skipped pulling the file if it wasn't in the resource index. That has the possibility of not being complete though, so this change removes that condition.

    Reviewed By: lambdapioneer

    Differential Revision: D3615436

    fbshipit-source-id: 559d61973a9848ae6e95767027f2250f2f8496e0