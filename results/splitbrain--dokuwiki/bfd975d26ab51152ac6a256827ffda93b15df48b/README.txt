commit bfd975d26ab51152ac6a256827ffda93b15df48b
Author: Andreas Gohr <andi@splitbrain.org>
Date:   Tue Nov 6 20:58:38 2012 +0100

    fix regression bug in HTTPClient FS#2621

    In the recent refactoring of the HTTPClient, a problem with certain
    systems was reintroduced. On these systems a select() call always
    waits for a timeout on the first call before working properly on the
    second call.

    This patch reintroduces the shorter timeouts with usleep rate limiting
    again.

    Since this bug is not reproducible on other systems it can't be unit
    tested unfortunately.