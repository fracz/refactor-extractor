commit 9f797b99cc5eb281cc7bd9237529b654bd13944f
Author: eliseosoto <eliseosoto@google.com>
Date:   Fri Jun 16 11:12:42 2017 -0700

    RefasterJS templates for replacing the usage of Array.prototype.indexOf to determine whether an element is in the array or not with Array.prototype.includes.

    This refactoring covers the most common cases in which this can be expressed:

    'arr.includes(elem)' is equivalent to:
      - arr.indexOf(elem) != -1
      - arr.indexOf(elem) !== -1
      - arr.indexOf(elem) > -1
      - arr.indexOf(elem) >= 0

    '!arr.includes(elem)' is equivalent to:
     - arr.indexOf(elem) == -1
     - arr.indexOf(elem) === -1
     - arr.indexOf(elem) < 0

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=159250600