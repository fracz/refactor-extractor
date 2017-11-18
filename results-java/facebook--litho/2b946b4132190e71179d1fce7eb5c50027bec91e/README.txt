commit 2b946b4132190e71179d1fce7eb5c50027bec91e
Author: Pascal Hartig <realpassy@fb.com>
Date:   Mon Jun 12 15:06:36 2017 -0700

    Migrate LayoutStateCalculateTest to AssertJ

    Summary:
    It's one of our biggest test suites and this should improve both readability and
    clarity of error messages if something goes wrong.

    Reviewed By: IanChilds

    Differential Revision: D5227738

    fbshipit-source-id: 58b2f7676a8ea5b3d78b8f86854d4c0a7cf9f5a9