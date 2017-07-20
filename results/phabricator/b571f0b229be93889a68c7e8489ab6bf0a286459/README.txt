commit b571f0b229be93889a68c7e8489ab6bf0a286459
Author: epriestley <git@epriestley.com>
Date:   Wed Mar 7 13:18:00 2012 -0800

    Forbid mailing list names contianing spaces or commas

    Summary:
    They end up in "CCs:" fields where they can't be parsed.

    Not bothering to migrate since I think only Dropbox has hit this.

    Also improved another error condition's handling.

    Test Plan: Tried to save a mailing list with spaces and commas in the name.

    Reviewers: btrahan, Makinde

    Reviewed By: btrahan

    CC: aran, epriestley

    Maniphest Tasks: T947

    Differential Revision: https://secure.phabricator.com/D1813