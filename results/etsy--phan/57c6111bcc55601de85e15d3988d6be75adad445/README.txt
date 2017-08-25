commit 57c6111bcc55601de85e15d3988d6be75adad445
Author: Tyson Andre <tysonandre775@hotmail.com>
Date:   Sun Apr 23 18:35:12 2017 -0700

    Check for redundant analysis steps in non-quick recursion.

    For issue #592

    A single-process test run on drupal had the duration of analysis reduced
    from 75 seconds to 60 seconds
    (Most of that improvement may be due to skipping
    over function calls where there are 0 parameters)

    - Note that if parameters are optional, and something is invoked with 0
      params, the parameter_list will still have parameters
      (based on defaults and original type)
    - This changes the behavior of non-quick mode. A different set of issues
      will be emitted as a result (of similar quality)
      (Previously, the recursion depth check was implemented incorrectly)
      Also, some similar issues with different union types (for different
      calling locations) may be emitted for the same lines of code.
    - This also accounts for the recursion depth in checking for redundant
      calls - If a new call would recurse deeper than the old one, then the
      new call isn't redundant.
    - Continue to always recurse for calls to functions with reference parameters.
      Those should be infrequent enough that they don't hurt performance.
      tests/rasmus_files/src/0039_static_property_ref.php somewhat covers
      the expected behavior already.
    - I checked, and visitMethod on a Method has self::$recursion_depth == 0 to start

    Update NEWS