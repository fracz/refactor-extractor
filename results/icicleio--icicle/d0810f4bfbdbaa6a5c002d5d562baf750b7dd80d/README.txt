commit d0810f4bfbdbaa6a5c002d5d562baf750b7dd80d
Author: Aaron Piotrowski <aaron@trowski.com>
Date:   Fri May 22 23:37:42 2015 -0500

    Prevent unnecessary exception construction on normal close.

    Socket closing methods were reorganized to prevent creating an exception unless a promise is pending and needs to be rejected.