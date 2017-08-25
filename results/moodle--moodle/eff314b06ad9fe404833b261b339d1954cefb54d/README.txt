commit eff314b06ad9fe404833b261b339d1954cefb54d
Author: David Mudrak <david@moodle.com>
Date:   Thu Nov 3 13:44:41 2011 +0100

    MDL-29920 Performance improvements in the gradebook reports

    Both methods get_activity_link() and get_grade_analysis_url() now cache
    the results of the check whether the activity module provides grade.php
    or not. Previously, get_activity_link() called file_exists() in every
    call and get_grade_analysis_url() did not check the existence at all.

    Note that this solution is still sub-optimal as apparently both methods
    do very similar job. This should be refactored one day by a brave
    developer who works on gradebook. Meanwhile, even this suboptimal
    solution is more effective than the previous one for most courses.