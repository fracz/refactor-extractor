commit 158b7be58a873cbc335f3de0a03778f9128cdf7b
Author: Uwe Tews <uwe.tews@googlemail.com>
Date:   Mon Nov 3 22:27:32 2014 +0100

    Debug Console Update

    - bugfix Debug Console did not show included subtemplates since 3.1.17
    (forum 25301)
    - bugfix Modifier debug_print_var did not limit recursion or prevent
    recursive object display at Debug Console
    (ATTENTION: parameter order has changed to be able to specify maximum
    recursion)
    - bugfix Debug consol did not include subtemplate information with
    $smarty->merge_compiled_includes = true
    - improvement The template variables are no longer displayed as objects
    on the Debug Console
    - improvement $smarty->createData($parent = null, $name = null) new
    optional name parameter for display at Debug Console
    - addition of some hooks for future extension of Debug Console