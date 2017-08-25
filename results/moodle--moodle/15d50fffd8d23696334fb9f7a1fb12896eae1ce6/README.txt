commit 15d50fffd8d23696334fb9f7a1fb12896eae1ce6
Author: Marina Glancy <marina@moodle.com>
Date:   Wed Feb 27 11:35:35 2013 +1100

    MDL-38147 Performance improvements to coursecat class:

    - Retrieve and cache only often-used fields of course category
    - Removed function coursecat::get_all_visible() as potentially causing performance issues
    - removed function coursecat::get_all_parents() as ineffective and unnecessary, replaced with get_parents()
    - retrieve all fields from course_categories when unretrieved field is accessed

    Also some code improvements:
    - rename functions starting with _ , rename arguments, etc.