commit bb45fe62c497f20232d722f70da0faac9fc15aba
Author: tjhunt <tjhunt>
Date:   Mon Mar 30 02:21:27 2009 +0000

    get_string: Refactoring, performance improvements, bug fixes and unit tests

    MDL-18669 get_string refactored to elimiate duplicate code and make it easier to understand.
    MDL-17763 parent language not processed correctly when getting a plugin string.
    MDL-16181 more intelligent caching to avoid repeated file_exists checks.
    MDL-12434 move values to array keys to improve lookup times.

    The main part of the refactoring is to create a singleton string_manager class to encapsulate the cached data and the processing, while breaking the code up into more smaller methods.

    Other performance improvements include:
    * Cache results of plugin name -> locations to search array.
    * Cache parent lang lookup.
    * Skip eval if the string does not contain $ \ or %.
    * Remove the unnecessary sprintf from the eval.

    There is a performance testing script in lib/simpletest/getstringperformancetester.php. For now this script has the old get_string implementation copied and pasted to the end, and renamed to old_get_string to allow for comparitive timings.

    There are now some unit tests for get_string in lib/simpletest/teststringmanager.php. I think I have managed to cover most of the tricky cases.