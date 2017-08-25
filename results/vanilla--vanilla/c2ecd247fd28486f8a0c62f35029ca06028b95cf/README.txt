commit c2ecd247fd28486f8a0c62f35029ca06028b95cf
Author: Todd Burry <todd@vanillaforums.com>
Date:   Fri Feb 21 16:03:27 2014 -0500

    Made some security improvements to password request.

    - Use a longer password reset key.
    - Make password reset requests expire after 1 hour.
    - Display a link to the password reset request form on an error.