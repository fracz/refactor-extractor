commit 14519f84f54b0100fcadb334220c711cfa8242b1
Author: Georgios Kalpakas <kalpakas.g@gmail.com>
Date:   Mon Jul 4 10:08:44 2016 +0300

    docs(copy): mention ignoring non-enumerable properties

    This also improves the example a bit:
    - better code formatting
    - initialization of `form` to an empty object
    - avoid using `email`, which doesn't get coppied when invalid (and might confuse users)

    Fixes #14853