commit 9ea6ac510565388c7db040c2bbfe6072cdbd6d88
Author: stronk7 <stronk7>
Date:   Tue May 31 22:35:04 2005 +0000

    Complete refactor of quiz_delete_course()

    Now this function works following this:
    - Iterate over every quiz category in the course (following parent-child relationships).
    - If the category is being used*, move it to site level (under a container category) and
          mark it as published.
    - If the category isn't being used, remove** it completely (questions, instances...) and
          re-parent its child categories.
    - Feedback is shown in a table detailing all the changes performed.

    * One category is being used if it has one question that is being used in any quiz,
      independently of its publish status.
    ** Removal of categories (and associated questions) has benn improved but, it won't
      be perfect until bug 3366 was solved.

    Tested against some large courses with reused questions and multiple levels of
    categories. Seems to work but

    PLEASE TEST IT AND SEND ANY FEEDBACK TO BUG 2459
    (http://moodle.org/bugs/bug.php?op=show&bugid=2459)

    Exactly this function is going to be used in the upgrade script to solve
    the orphan categories issue and it must work perfectly!

    Merged from MOODLE_15_STABLE