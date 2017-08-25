commit 9152fc9938105282329a7cd4605ce22a573651c4
Author: skodak <skodak>
Date:   Sun Jan 28 21:18:08 2007 +0000

    MDL-8323 finished full conversion to proper $COURSE global - no more $CFG->coursetheme, $CFG->courselang - improved course_setup(), current_language() and current_theme(); and Chameleon theme fixes needed for global $COURSE
    MDL-7962 chat seems to be completely broken in head (fixed wrong JS)
    * reworked chat themes support
    MDL-8338 Cron does not need cookies
    MDL-8339 forum cron capabilities problems
    * minor deprecated function current_encoding() cleanup