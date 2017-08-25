commit fd04c5bd87ca76601962ec9ba09a904e21d64008
Author: David Mudrak <david@moodle.com>
Date:   Wed Sep 28 02:02:37 2011 +0200

    MDL-27857 Export to portfolio support in the assignment module improved

    Portfolio API code in the assignment module expected that the current
    user is the author of the submission. Therefore the "Export to
    portfolio" button did not work when the submission was viewed by a
    teacher (eg at the page with the list of all submissions in the Advanced
    upload assignment).

    This patch introduces a new callback argument 'submissionid' that holds
    explicit ID of the submission the export deals with. With it available,
    we do not need to expect the current user is the author of the
    submission.

    The patch also cleans some strings used for portfolio callback
    exceptions.