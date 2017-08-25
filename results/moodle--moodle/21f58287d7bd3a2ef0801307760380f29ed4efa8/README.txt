commit 21f58287d7bd3a2ef0801307760380f29ed4efa8
Author: David Mudrak <david@moodle.com>
Date:   Fri Apr 13 16:30:14 2012 +0200

    MDL-27508 workshop: improved getting of potential authors and reviewers

    This patch reimplements get_potential_authors() and get_potential_reviewers()
    so that get_enrolled_sql() is used instead of get_users_by_capability().
    This excludes non-enrolled users (or users with suspended enrolment)
    from the list of potential users.

    The patch also extends the returned user structure. Objects in the
    returned collection are now suitable for user_picture renderer.