commit ee07a54be05887aebb47eff06684af427026248c
Author: Damyon Wiese <damyon@moodle.com>
Date:   Wed Sep 24 16:21:30 2014 +0800

    MDL-47064 Grades: Peer review cleanups

    Changes include:
     * Search for existing items to reduce DB queries in grade_category::aggregate_grades
     * Comments improvements
     * Move brackets to be part of lang string
     * Convert aggregationhints to be a class variable instead of passing it around

    Part of: MDL-46576