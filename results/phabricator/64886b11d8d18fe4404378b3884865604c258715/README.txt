commit 64886b11d8d18fe4404378b3884865604c258715
Author: epriestley <git@epriestley.com>
Date:   Sun Jul 31 09:25:24 2016 -0700

    Remove expensive, pointless typeachecking in custom fields

    Summary:
    Ref T11404. On my system, this improves performance by 10-15% for `differential.revision.search`.

    `PhutilTypeSpec` provides high quality typechecking and is great for user-facing things that need good error messages.

    However, it's also a bit slow, and pointless here (the API is internal and it only has one possible option).

    I think I added this after writing `checkMap` just because I wanted to use it more often. My desire is sated after finding many reasonable ways to use it to give users high-quality error messages about things like configuration files.

    Test Plan: Profiled `differential.revision.search` before and after change, saw wall time drop from ~220ms to ~195ms.

    Reviewers: yelirekim, chad

    Reviewed By: chad

    Maniphest Tasks: T11404

    Differential Revision: https://secure.phabricator.com/D16354