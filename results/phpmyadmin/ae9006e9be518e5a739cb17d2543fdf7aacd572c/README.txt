commit ae9006e9be518e5a739cb17d2543fdf7aacd572c
Author: Michal Čihař <michal@cihar.com>
Date:   Thu May 4 14:24:05 2017 +0200

    Use one temporary directory

    Share one directory for Twig cache, SHP import and file uploads.

    The code now also validates the cache directory and creates it on the
    fly, so it properly detects if the directory can not be used.

    Also the documentation has been improved to document securing this
    directory.

    Fixes #13225
    Fixes #13226

    Signed-off-by: Michal Čihař <michal@cihar.com>