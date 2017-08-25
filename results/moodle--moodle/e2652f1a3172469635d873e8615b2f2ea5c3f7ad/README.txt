commit e2652f1a3172469635d873e8615b2f2ea5c3f7ad
Author: Marina Glancy <marina@moodle.com>
Date:   Wed Mar 21 13:55:22 2012 +0800

    MDL-27236: Server files, improve tree view

    - Removed 'Private files' from 'Server files' repository;
    - Show only non-empty directories (taking into account filetype filter);
    - If there is only one non-empty filearea within a module, do not show it and skip the extra subdirectory level;
    - If the user is not admin (does not have 'moodle/course:update' capability in system context), do not show course categories, just list available courses;
    - Also when retrieving the course files capability to managefiles is checked before retrieving the modules list for performance tuning on sites with a lot of courses