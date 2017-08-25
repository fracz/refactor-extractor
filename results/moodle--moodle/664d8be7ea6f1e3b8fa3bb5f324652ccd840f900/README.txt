commit 664d8be7ea6f1e3b8fa3bb5f324652ccd840f900
Author: Mark Nelson <markn@moodle.com>
Date:   Tue Jan 19 17:04:56 2016 +0800

    MDL-48634 core: prevent change of grade values when necessary

    Three additional checks have been added.

    Once grades have been recorded for the activity/grade item -

    1) Do not allow the grade type to be changed.
    2) Do not allow the scale to be changed.
    3) If we are using ratings do not allow the 'Maximum points'
    value to be changed.

    Also reordered form elements, removed form elements that
    were not necessary, added and changed existing language
    strings to improve the overall UI.