commit 3794c6cc9beee13e0a7b8aed6b10290df05d3961
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Tue Jul 24 11:19:37 2012 -0500

    [#1959] CS review

    - Removed most usage of ArrayUtils -- most arrays were single
      dimensional, and did not require more than iterator_to_array
    - Alphabetized all imports
    - Some logic cleanup for readability
    - Marked filter and writer plugin managers to NOT share by default
      (allowing multiple instances of the same writer and filter types)