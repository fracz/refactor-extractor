commit 9da88a99daca0cc490a758956ec92a5fbe69abcf
Author: Lee Hinman <lee@writequit.org>
Date:   Fri Apr 22 11:48:05 2016 -0600

    Fix exit code

    Exit with proper exit code (1) and an error message if elasticsearch
    executable binary does not exists or has insufficient permissions to
    execute.

    Squashed commit of the following:

    commit 9768d316303418ba4f9c96d3f87c376048a1b1bc
    Author: Puru <tuladharpuru@gmail.com>
    Date:   Fri Apr 22 23:26:47 2016 +0545

        Fixed ES_HOME typo

    commit 79a2b0394297f8b02b6f71b71ba35ff79f1a684e
    Author: Puru <tuladharpuru@gmail.com>
    Date:   Sun Apr 10 11:00:24 2016 +0545

        Improve elasticsearch startup script test

        Added improvement as per conversation in https://github.com/elastic/elasticsearch/pull/17082#issuecomment-206459613

    commit 7be38e1fefd4baa6ccdbdc14745c00f6dc052e0c
    Author: Puru <tuladharpuru@gmail.com>
    Date:   Wed Mar 23 13:23:52 2016 +0545

        Add elasticsearch startup script test

        The test ensures that elasticsearch startup script exists and is executable.

    commit d10eed5c08260fa9c158a4487bbb3103a8d867ed
    Author: Puru <tuladharpuru@gmail.com>
    Date:   Wed Mar 23 12:30:25 2016 +0545

        Fixed IF syntax and failure message

    commit 6dc66f616545572485b4d43bee05a4cbbf1bed72
    Author: Puru <tuladharpuru@gmail.com>
    Date:   Sat Mar 12 11:08:11 2016 +0545

        Fix exit code

        Exit with proper exit code (1) and an error message if elasticsearch executable binary does not exists or has insufficient permissions to execute.