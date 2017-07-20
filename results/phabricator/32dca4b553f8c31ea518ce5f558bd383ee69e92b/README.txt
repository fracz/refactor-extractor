commit 32dca4b553f8c31ea518ce5f558bd383ee69e92b
Author: epriestley <git@epriestley.com>
Date:   Thu Oct 17 11:21:01 2013 -0700

    Fix lightbox downloads for embeded images and a warning

    Summary:
    I refactored this recently and accidentally dropped the download URI.

    Also fix a warning with, e.g., files named `README`.

    Test Plan: Clicked a thumb, clicked "Download", got a file.

    Reviewers: chad, btrahan, dctrwatson

    Reviewed By: chad

    CC: aran

    Differential Revision: https://secure.phabricator.com/D7341