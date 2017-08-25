commit 4f11517667165380b217ac62737cae02b7f79c4e
Author: David Mudrák <david@moodle.com>
Date:   Sat Oct 20 00:05:38 2012 +0800

    MDL-36135 Give the grading evaluation methods control over their settings forms

    From now on, the evaluator's method get_settings_form() should return a
    subclass of workshop_evaluation_settings_form. The evaluation subplugins
    are expected to use the define_sub() method to add their own fields into
    the base form, although they can override the main define() method, too.

    The former interface workshop_evaluation has been refactored into a
    superclass with abstract methods which seems to be more robust.

    Oh, by the way, I'm in Perth - yay!

    AMOS BEGIN
     MOV [settings,workshopeval_best],[evaluationsettings,mod_workshop]
    AMOS END