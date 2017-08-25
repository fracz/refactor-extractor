commit 2d22f3d97f30d4ab825e0caf2cb84a10b056f8cb
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Mon Jul 29 16:03:58 2013 +0100

    MDL-27655 improve purge all caches.

    If you click the link in the page footer, then it will reliably
    redirect you back to the page you were on after purging the caches.

    If you go to the purge all caches page in the admin menu, it shows you a
    purge button, with no cancel button. Clicking the button purges the
    caches and takes you back to the page with the button.