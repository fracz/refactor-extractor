commit d20e4571215b379676bf95898232717b121ad95b
Author: Dries Buytaert <dries@buytaert.net>
Date:   Mon Jan 19 10:46:52 2009 +0000

    - Patch #201122 by c960657, Moshe Weitzman: never write anonymous sessions, unless something has been written to . This is an important performance improvements -- as long as you use modules that use  carefully. It might be good to report some analytics about this in the performance settings page so administrators can see if there is a 'broken' module.