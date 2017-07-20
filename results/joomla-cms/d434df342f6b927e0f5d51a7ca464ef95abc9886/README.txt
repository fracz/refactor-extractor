commit d434df342f6b927e0f5d51a7ca464ef95abc9886
Author: Frank Mayer <frank@frankmayer.net>
Date:   Thu Jun 8 09:38:43 2017 +0300

    Performance 4 (plugins) (#12228)

    * Use Elvis instead of full ternary

    * Use prefixed increment instead of assignment

    * Use single quotes instead of doublequotes

    * Remove unnecessary parentheses

    * Replace alias function with original

    * Remove useless return

    * Removed unnecessary ternary operators

    * Fixed callable call in loop termination condition

    *       Replace is_null() with ... === null

    * Shortened syntax for applied operations

    * Don't use strlen() to check if string is empty.

    * Replace strstr() with strpos()

    * Replace substr() with strpos()

    * Merge unset()

    * Inline one-time use variables

    * Type safe string comparison

    * Remove unused variable

    * Merge if statements into parents

    * Optimize non-optimal if conditions.

    * More type-safe comparisons

    * Fix another non-optimal if condition

    * Merged another  str_replace() case

    * Fix own typo

    * Fix formatting

    * Further optimize an if condition

    * Fix codesniffer

    * Fixes...

    * Changes, based on discussions with @csthomas

    * One more change, based on previous discussion.

    * A few more minor fixes

    * Fix slipped through merge conflict

    * Correction in comparison

    * Correction in comparison with trim result

    * Reverted this one to not clash with @laoneo's refactoring efforts

    * Some changes according to reviewer's comments

    * Some changes according to reviewer's comments

    * Additional changes according to reviewer's comments