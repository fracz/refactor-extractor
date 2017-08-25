commit 25ef0370d5bdc8e6a61ffb3ba9d0b970828cfa10
Author: stronk7 <stronk7>
Date:   Mon Jul 4 18:56:03 2005 +0000

    Improved automatic relinking in backup and restore. Credits go to skodak.
    Complete refactoring of the system that was really awful (my fault!).
    Now everything is in its place and working like a charm, making things really
    easier to be implemented and amplied. Bug 3678
    (http://moodle.org/bugs/bug.php?op=show&bugid=3678)
    (http://moodle.org/mod/forum/discuss.php?d=26530)

    Note: Everything is merged from stable but the quiz module because it has
    other changes not merged for now. I've skyped a message to Gustav about it.

    Merged from MOODLE_15_STABLE