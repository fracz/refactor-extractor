commit 67e1f26874cf11af8ef6d6da50e66f27504bfa01
Author: Sam Hemelryk <sam@moodle.com>
Date:   Tue Oct 1 09:02:08 2013 +1300

    MDL-31830 course: several small code improvements

    * I can rebase this for you/the integrator before it goes in for sure no probs :)
    * Fixed double phpdoc block of course_change_visibility
    * Moved permission checks out of course_move_after_course and into helper function.
    * Reviewed setType calls for editcategory_form.php.
    * Reviewed all uses of can_resort and added more specific methods.
    * Fixed method mentioned in exception for resort methods.
    * Converted calls to fetch courses to call get_course.
    * Exceptions now thrown when trying to move courses and problems arise.
    * Fixed unnecessary namespace hinting in core_course_management_renderer.
    * Abstracted common logic of can_resort_any and can_change_parent_any.
    * Removed check for system level capability from has_manage_capability_on_any.
    * Reviewed debugging calls I've introduced.