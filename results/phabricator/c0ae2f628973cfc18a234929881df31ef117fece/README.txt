commit c0ae2f628973cfc18a234929881df31ef117fece
Author: epriestley <git@epriestley.com>
Date:   Sun Jul 17 11:06:02 2011 -0700

    Show change diffs in Phriction

    Summary:
    This is really rough and needs work (particularly, there's some diff code I
    really need to refactor since I sort-of-copy-pasted it) but basically
    functional.

    Show text changes between diffs and allow users to revert to earlier versions.

    Differential's line-oriented diff style isn't ideal for large blocks of text but
    I'm betting this is probably good enough in most cases. We can see how bad it is
    in practice and then fix it if needbe.

    I added a bunch of support for "description" but didn't add the feature in this
    diff, I'll either follow up or task it out since it should be a pretty
    straightforward change.

    Test Plan: Looked at history for several Phriction documents, clicked "previous
    change" / "next change", clicked revert buttons.
    Reviewed By: hsb
    Reviewers: hsb, codeblock, jungejason, tuomaspelkonen
    CC: aran, hsb, epriestley
    Differential Revision: 687