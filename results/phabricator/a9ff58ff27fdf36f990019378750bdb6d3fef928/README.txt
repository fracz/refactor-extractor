commit a9ff58ff27fdf36f990019378750bdb6d3fef928
Author: epriestley <git@epriestley.com>
Date:   Wed Jan 16 09:01:16 2013 -0800

    Add even more Differential options

    Summary:
    These are technically in MetaMTA right now, but I put them in Differential since I think it's probably a better primary category fit and having 120 options in MetaMTA won't do anyone any favors. (We can do some kind of cross-categorization or "related options" later if we get feedback that people are having trouble finding options like these which have multiple reasonable categories.)

    Also improve the readability of displayed JSON literals with forward slashes.

    Test Plan: Looked at options, edited a couple of them. Looked at JSON literal values, saw them rendered more readably.

    Reviewers: codeblock, btrahan

    Reviewed By: codeblock

    CC: aran

    Maniphest Tasks: T2255

    Differential Revision: https://secure.phabricator.com/D4464