commit 60f20dc23cb08d598d605f51f7526725ec42abd9
Author: jwilson <jwilson@squareup.com>
Date:   Tue Dec 31 01:15:15 2013 -0500

    Remove more stuff from Policy.

    Policy is a bridge to get us from a place where HttpEngine
    depends 100% on HttpURLConnection to a place where it
    doesn't.

    We've refactored the code in HttpEngine enough now that
    some Policy methods are no longer necessary. Delete 'em.