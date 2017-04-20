commit 7780f018fb42888959bffbbb33ce56b5d505d6d8
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Tue Jun 26 13:37:07 2012 -0500

    [zen-18][#1585] CS review

    - Fixed a variety of CS issues
    - Translated a number of overloaded methods that acted as combination
      setters/getters into discrete mutators and accessors
    - Need to refactor to use service manager
    - Replaced a number of exit() calls by throwing exceptions
    - s/array_key_exsits/array_key_exists/
    - s/getLeftDenoation/getLeftDenotation/