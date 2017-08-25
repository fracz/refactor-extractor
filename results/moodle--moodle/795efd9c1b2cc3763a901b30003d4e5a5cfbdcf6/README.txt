commit 795efd9c1b2cc3763a901b30003d4e5a5cfbdcf6
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Fri May 13 20:36:30 2016 +0100

    MDL-54582 accesslib: improve performance of load_course_context

    The new query is logically equivalen, but much, much faster, at
    least on Postgred. (15ms, instead of 700ms, in one example I tried
    on the database for the OU's main Moodle site.)