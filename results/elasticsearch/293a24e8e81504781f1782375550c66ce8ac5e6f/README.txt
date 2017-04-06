commit 293a24e8e81504781f1782375550c66ce8ac5e6f
Author: Ryan Ernst <ryan@iernst.net>
Date:   Tue Oct 18 18:36:16 2016 -0700

    Plugins: Add back user agent when downloading plugins (#20872)

    This adds back a User-Agent header, which we had before large plugin
    script refactorings for 5.0. The value is now
    `elasticsearch-plugin-installer`.