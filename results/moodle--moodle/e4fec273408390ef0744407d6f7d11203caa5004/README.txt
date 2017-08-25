commit e4fec273408390ef0744407d6f7d11203caa5004
Author: martinlanghoff <martinlanghoff>
Date:   Wed Sep 19 07:13:33 2007 +0000

    accesslib: Introducing context_moved() - call after moving courses/coursecats

    With the new accesslib, moving courses and categories has a major
    impact on enrolments and unenrolments.

    At _least_ we need to signal accesslib that it has happened. So here
    is context_moved() for exactly that.

    Open to refactoring later into something along the lines of

     - move_course()
     - move_category()

    However, at this stage the most important of those two: move_course()
    does not fit very well with the code in course/edit. So keep it simple
    for now.