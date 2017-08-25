commit ddcfda87d6f3cc4dfec9947a15bee84b880a76a6
Author: Charles Severance <csev@umich.edu>
Date:   Tue Nov 15 12:46:48 2011 -0500

    MDL-20534

    I ran the software through the certification and caught a few nits:

    The error return is 'failure' not 'error'
    The spec says that it needs to return 'failure' for out of range or non-numeric grades
    The result score needs a language tag, hard-coded as 'en'
    Setting a grade multiplied by 100 but reading the grade did not divide by 100
    All those are now fixed with this patch as well as this bit of cruft:

    I removed the "extension service url" as it is not implemented in service.php

    Feel free to review and adjust - probably the one place you might want to refactor
    is that I put code to catch out-of-range-and non-numeric in
    lti_parse_grade_replace_message and threw an exception on error and then caught
    it in service.php and sent back the 'failure' message. Feel free to refactor a
    bit if you see this done in a cleaner manner.