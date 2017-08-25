commit bf1405a60ad2c4230f3023f66ba479ec5d588bb1
Author: Marina Glancy <marina@moodle.com>
Date:   Fri Sep 27 15:49:24 2013 +1000

    MDL-42020 course: performance improvement in course listings

    - better performance when get_course_count() OR search_course_count() are called by themselves
    - better performance in displaying course contacts