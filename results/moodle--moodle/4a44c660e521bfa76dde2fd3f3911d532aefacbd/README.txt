commit 4a44c660e521bfa76dde2fd3f3911d532aefacbd
Author: David Mudrák <david@moodle.com>
Date:   Wed Sep 17 23:04:42 2014 +0200

    MDL-31936 workshop: Delete attachments on record removal

    The methods workshop::delete_submission() and workshop::delete_assessment() did
    not delete files (embedded and attachments) associated with the given
    submission or assessment. This is fixed now.

    Additionally, the delete_assessment() method now cleans-up records from the
    table workshop_grades, too. This internal workshop API still does not give
    workshop subplugins a chance to clean up their data, should they store them in
    their own tables instead of the workshop_grades one. This should be improved in
    the future yet.