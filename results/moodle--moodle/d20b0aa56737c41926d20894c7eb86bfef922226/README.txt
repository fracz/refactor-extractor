commit d20b0aa56737c41926d20894c7eb86bfef922226
Author: Dan Poltawski <dan@moodle.com>
Date:   Wed Aug 14 10:46:19 2013 +0800

    MDL-41157 calendar: improve calendar_get_course_cached

    The function get_course() introduced in 2.5.1 will use $COURSE and
    $SITE to avoid an uncessary DB query, simplifying the logic of this
    function and improving perf.