commit f8ea60777cbee3ef2b44b6bc46f45e525c567130
Author: martinlanghoff <martinlanghoff>
Date:   Wed Sep 12 02:56:16 2007 +0000

    fix_course_sortorder(): fix breakage with large categories, saner error

    Two improvements for fix_course_sortorder()

     - If the category has more courses than the shift range
       use a larger shift range to avoid overlapping with itself

     - If things do go wrong during the per-course sortorder updates,
       rollback and try and call ourselves with a 'safe' flag.

    Still - far from perfect. Probably the global sortorder approach
    is broken. The sanest way is to rework things to always join against
    course_categories and order by the combined sortorders.