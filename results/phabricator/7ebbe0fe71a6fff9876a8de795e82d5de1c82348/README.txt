commit 7ebbe0fe71a6fff9876a8de795e82d5de1c82348
Author: epriestley <git@epriestley.com>
Date:   Thu Sep 3 10:03:50 2015 -0700

    Add a "Printable Version" link to Phortune invoices

    Summary:
    Ref T9309. This is a minor quality of life improvement, hopefully. We already have print CSS, just expose it more clearly.

    Also, hide actions (these never seem useful?) and footers from printable versions. I opened the printable version in a new window since it now doesn't have any actions.

    Test Plan:
    {F777241}

    {F777242}

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T9309

    Differential Revision: https://secure.phabricator.com/D14045