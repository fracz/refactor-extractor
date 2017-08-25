commit 97d2756c65f516ab75e3801e5d9d0c4269266968
Author: mark-nielsen <mark-nielsen>
Date:   Sat Mar 25 21:38:57 2006 +0000

    [New Feature] Tabbed navigation added to lesson (similar to quiz)
    [Added] Graceful fails added to view.php, essay grading, and reports
    [Changed] got rid of a lot of references to $_POST/GET/REQUEST
    [Fixed] In reports, it required teacheredit, should only be teacher
    [Changed] High scores - teachers can actually see them!  Also, improved output by using print_table and print_heading
    [Changed] Grade essays now uses fullname() to print user names