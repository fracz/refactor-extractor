commit 3e068a34fc386ccf8dafdd921d9e712faa3aa075
Author: Ismayil Khayredinov <ismayil.khayredinov@gmail.com>
Date:   Wed Sep 28 18:26:14 2016 +0200

    chore(navigation): update the use of breadcrumbs in layout views

    Removes nav parameter from layout views and improves the use of
    breadcrumbs parameter.
    Adds current route identifier and segments to the prepare, breadcrumbs
    hook params.
    Allows injecting breadcrumbs into elgg_get_breadcrumbs()

    more