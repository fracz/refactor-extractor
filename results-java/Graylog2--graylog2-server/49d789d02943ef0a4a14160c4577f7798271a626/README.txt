commit 49d789d02943ef0a4a14160c4577f7798271a626
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Fri Aug 30 00:11:22 2013 +0200

    slightly refactor Api class

    now uses the current user for credentials, except if they are passed in explicitly, which is only necessary for login
    the URL is now assembled by using the URI class, which properly escapes the necessary portions.
    once we switch to messageformat based rest resource description, we can get rid of most explicit string operations on urls