commit 7d152def3e59f72a3a5767830db8e49672afec70
Author: epriestley <git@epriestley.com>
Date:   Mon Jul 11 20:14:46 2011 -0700

    Basic remarkup integration for Phriction

    Summary: Document linking and some general layout improvements. I'd like to
    eventually do more meta-dataey things with links (like store them separately and
    check them for 404s) but this is a decent start.
    Test Plan:
    https://secure.phabricator.com/file/view/PHID-FILE-d756b94a06b69c273fce/
    Reviewed By: jungejason
    Reviewers: hsb, codeblock, jungejason, tuomaspelkonen, aran
    CC: aran, jungejason, epriestley
    Differential Revision: 650