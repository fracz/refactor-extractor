commit cf6155226cba2e4e62e9e1cb44dad88afacc39b4
Author: tjhunt <tjhunt>
Date:   Fri Jun 12 12:13:07 2009 +0000

    ajaxlib/require_js: MDL-16693 $PAGE->requires->... deprecates require_js etc.

    There is a new implementation of require_js in lib/deprecatedlib.php,
    based on $PAGE->requires.

    There were a few other recently introduced functions in lib/weblib.php,
    namely print_js_call, print_delayed_js_call, print_js_config and
    standard_js_config. These have been removed, since they were never in
    a stable branch, and all the places that used them have been changed
    to use the newer $PAGE->requires->... methods.

    get_require_js_code is also gone, and the evil places that were calling
    it, even though it is an internal function, have been fixed.

    Also, I made some minor improvements to the code I committed yesterday
    for MDL-16695.

    All that remains is to update all the places in core code that are
    still using require_js.

    (This commit also fixes the problem where the admin tree would not
    start with the right categories expanded.)