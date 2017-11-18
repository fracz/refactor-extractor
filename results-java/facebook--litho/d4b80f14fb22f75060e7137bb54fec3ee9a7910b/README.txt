commit d4b80f14fb22f75060e7137bb54fec3ee9a7910b
Author: Pascal Hartig <realpassy@fb.com>
Date:   Mon Sep 11 08:16:07 2017 -0700

    Add initial TestSpec generator

    Summary:
    This is very much a work in progress. There are a bunch of issues with this
    code; most noticeably code duplication, dead code and useless generated output.

    It is not wired up yet and not *expected* to generate something useful. I want
    to ship some pieces now, to reduce the rebase churn, then add more features and
    refactor along the way.

    Especially the logic for the resource extraction seems awfully repetitive, but I
    don't want to generalize this before I know for sure what code I will actually
    end up using.

    Reviewed By: IanChilds

    Differential Revision: D5785545

    fbshipit-source-id: de74322ce8a81a88aeebf35d06a01e21884ff9de